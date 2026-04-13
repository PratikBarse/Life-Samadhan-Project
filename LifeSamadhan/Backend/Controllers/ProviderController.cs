using LifeSamadhan.API.Data;
using LifeSamadhan.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Linq;
using System.Security.Claims;
using LifeSamadhan.API.Models;
using Microsoft.EntityFrameworkCore;

namespace LifeSamadhan.API.Controllers
{
    [Authorize( Roles="SERVICEPROVIDER,ADMIN")]
    [ApiController]
    [Route("api/provider")]
    public class ProviderController : ControllerBase
    {
        private readonly LifeSamadhanDbContext db;
        private readonly NotificationService _ns;

        public ProviderController(LifeSamadhanDbContext db, NotificationService ns)
        {
            this.db = db;
            this._ns = ns;
        }

        private long CurrentUserId()
        {
            var idClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(idClaim)) return 0;
            return long.Parse(idClaim);
        }

        private Models.ServiceProvider GetProviderForCurrentUser()
        {
            var uid = CurrentUserId();
            if (uid == 0) return null;
            return db.ServiceProviders.FirstOrDefault(p => p.UserId == uid);
        }

       
        [HttpPost("assignment/{assignmentId}/accept")]
        public async Task<IActionResult> Accept(long assignmentId)
        {
            var assignment = db.ServiceAssignments.Find(assignmentId);
            if (assignment == null)
                return NotFound("Assignment not found");

            var provider = GetProviderForCurrentUser();
            if (provider == null) return Forbid();
            if (assignment.ProviderId != provider.Id) return Forbid();

            if (assignment.Status != "ASSIGNED")
                return BadRequest("Request is no longer in ASSIGNED state");

            assignment.Status = "ACCEPTED";
            assignment.AcceptedAt = DateTime.UtcNow;
            assignment.RespondedAt = DateTime.UtcNow;   

            
            var req = db.ServiceRequests.Find(assignment.RequestId);
            if (req != null) 
            {
                req.Status = "ACCEPTED";
                await _ns.Send(req.CustomerId, $"Request #{req.Id} has been accepted by the provider!");
            }

            db.SaveChanges();

            return Ok(assignment);
        }

        
        [HttpPost("assignment/{assignmentId}/reject")]
        public async Task<IActionResult> Reject(long assignmentId)
        {
            var assignment = db.ServiceAssignments.Find(assignmentId);
            if (assignment == null)
                return NotFound("Assignment not found");

            var provider = GetProviderForCurrentUser();
            if (provider == null) return Forbid();
            if (assignment.ProviderId != provider.Id) return Forbid();

            if (assignment.Status != "ASSIGNED")
                return BadRequest("Request is no longer in ASSIGNED state");

            assignment.Status = "REJECTED";
            assignment.RespondedAt = DateTime.UtcNow;

            var req = db.ServiceRequests.Find(assignment.RequestId);
            if (req != null)
            {
                req.Status = "CANCELLED";
                await _ns.Send(req.CustomerId, $"Provider has rejected Request #{req.Id}. It is now cancelled.");
            }

            db.SaveChanges();

            return Ok(assignment);
        }

        
        [HttpPost("assignment/{assignmentId}/start")]
        public async Task<IActionResult> StartService(long assignmentId, [FromBody] string otp)
        {
            var assignment = db.ServiceAssignments.Find(assignmentId);
            if (assignment == null)
                return NotFound("Assignment not found");

            var provider = GetProviderForCurrentUser();
            if (provider == null) return Forbid();
            if (assignment.ProviderId != provider.Id) return Forbid();

            if (assignment.Status != "ACCEPTED")
                return BadRequest("Service not yet accepted");

            if (assignment.Otp != otp)
                return BadRequest("Invalid OTP");

            assignment.Status = "STARTED";
            assignment.StartedAt = DateTime.UtcNow;

            
            var req = db.ServiceRequests.Find(assignment.RequestId);
            if (req != null) 
            {
                req.Status = "IN_PROGRESS";
                await _ns.Send(req.CustomerId, $"Provider has started work on Request #{req.Id}!");
            }

            db.SaveChanges();

            return Ok(assignment);
        }

      
        [HttpPost("assignment/{assignmentId}/complete")]
        public async Task<IActionResult> CompleteService(long assignmentId)
        {
            var assignment = db.ServiceAssignments.Find(assignmentId);
            if (assignment == null)
                return NotFound("Assignment not found");

            var provider = GetProviderForCurrentUser();
            if (provider == null) return Forbid();
            if (assignment.ProviderId != provider.Id) return Forbid();

            if (assignment.Status != "STARTED")
                return BadRequest("Service not started yet");

            assignment.Status = "COMPLETED";
            assignment.CompletedAt = DateTime.UtcNow;

            
            var duration = assignment.CompletedAt.Value - assignment.StartedAt.Value;
            double hours = Math.Max(1.0, Math.Round(duration.TotalHours, 2));
            
            
            var req = db.ServiceRequests.Find(assignment.RequestId);
            if (req != null) 
            {
                req.Status = "COMPLETED";
                req.CompletionDate = DateTime.UtcNow;
                
                if (provider.HourlyRate.HasValue)
                {
                    req.Amount = (decimal)(hours * (double)provider.HourlyRate.Value);
                }

                await _ns.Send(req.CustomerId, $"Service COMPLETED for Request #{req.Id}. Total Amount: ₹{req.Amount}");
            }

            db.SaveChanges();

            return Ok(new { assignment, hours, finalAmount = req?.Amount });
        }

      
        [HttpGet("assignments")]
        public IActionResult MyAssignments()
        {
            var provider = GetProviderForCurrentUser();
            if (provider == null) return NotFound("Provider profile not found");

            var assignments = db.ServiceAssignments
                .Where(a => a.ProviderId == provider.Id)
                .OrderByDescending(a => a.AssignedAt)
                .ToList();

            var result = assignments.Select(a => {
                a.Otp = "PROTECTED"; 
                var req = db.ServiceRequests.Find(a.RequestId);
                if (req != null) req.OTP = "PROTECTED"; 

                var svc = db.Services.Find(req?.ServiceId ?? 0);
                var cat = db.ServiceCategories.Find(req?.ServiceCategoryId ?? 0);
                var cust = db.Users.Find(req?.CustomerId ?? 0);

                return new
                {
                    assignment = a,
                    request = req,
                    service = svc != null ? new { name = svc.Name } : (cat != null ? new { name = cat.Name } : new { name = "General Service" }),
                    category = cat,
                    customer = cust != null ? new { name = cust.Name, mobile = cust.Mobile, email = cust.Email } : null,
                    rating = db.Ratings.FirstOrDefault(r => r.ServiceRequestId == a.RequestId)
                };
            });

            return Ok(result);
        }

       
        [HttpGet("profile")]
        public IActionResult GetProfile()
        {
            var provider = GetProviderForCurrentUser();
            if (provider == null) return NotFound("Provider profile not found");

            var user = db.Users.Find(provider.UserId);

            return Ok(new { user, provider });
        }

        
        [HttpGet("skills")]
        public IActionResult MySkills()
        {
            var provider = GetProviderForCurrentUser();
            if (provider == null) return NotFound("Provider profile not found");

            var skills = db.ProviderSkills
                .Where(s => s.ProviderId == provider.Id)
                .ToList()
                .Select(s => new { s.Id, s.ServiceId, s.Remarks, s.Status, Service = db.Services.Find(s.ServiceId) })
                .ToList();

            return Ok(skills);
        }

        
        [HttpPut("availability")]
        public IActionResult SetAvailability([FromBody] string availability)
        {
            var provider = GetProviderForCurrentUser();
            if (provider == null) return NotFound("Provider profile not found");

            provider.Availability = availability;
            db.SaveChanges();
            return Ok(provider);
        }

        [HttpGet("earnings")]
        public IActionResult GetEarnings()
        {
            var provider = GetProviderForCurrentUser();
            if (provider == null) return NotFound("Provider profile not found");

            var completedRequests = db.ServiceRequests
                .Include(r => r.Service)
                .Include(r => r.ServiceCategory)
                .Where(r => r.ProviderId == provider.Id && r.Status == "PAID")
                .ToList();

            var totalEarnings = completedRequests.Sum(r => r.Amount);
            var totalJobs = completedRequests.Count;

            var reqIds = completedRequests.Select(r => r.Id).ToList();
            var assignments = db.ServiceAssignments
                .Where(a => reqIds.Contains(a.RequestId) && a.Status == "COMPLETED")
                .ToList();

            double totalHrs = 0;
            foreach (var a in assignments)
            {
                if (a.StartedAt.HasValue && a.CompletedAt.HasValue)
                    totalHrs += (a.CompletedAt.Value - a.StartedAt.Value).TotalHours;
            }

            var historyData = completedRequests.Select(r => new
            {
                id = r.Id,
                amount = r.Amount,
                completionDate = r.CompletionDate ?? r.CreatedAt,
                service = r.Service?.Name ?? r.ServiceCategory?.Name ?? "General Service"
            }).ToList();

            
            var providerUser = db.Users.Find(provider.UserId);
            var ratings = db.Ratings.Where(r => r.RevieweeId == provider.UserId).ToList();
            double avgRating = ratings.Any() ? ratings.Average(r => r.Stars) : 0;

            return Ok(new
            {
                totalEarnings,
                totalJobs,
                totalHours = Math.Round(totalHrs, 2),
                averageRating = Math.Round(avgRating, 1),
                ratingCount = ratings.Count,
                history = historyData
            });
        }
    }
}
