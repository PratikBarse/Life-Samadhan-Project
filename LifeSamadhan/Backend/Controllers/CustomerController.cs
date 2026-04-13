using Microsoft.AspNetCore.Mvc;
using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using LifeSamadhan.API.Services;
using System;
using Microsoft.AspNetCore.Authorization;
using System.Linq;
using System.Security.Claims;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;

namespace LifeSamadhan.API.Controllers
{
    
    public class CustomerProfileUpdateDTO
    {
        public string? Name { get; set; }
        public string? Mobile { get; set; }
        public string? Address { get; set; }
        public long? LocationId { get; set; }
    }

    [Authorize(Roles = "CUSTOMER,ADMIN")]
    [ApiController]
    [Route("api/customer")]
    public class CustomerController : ControllerBase
    {
        private readonly LifeSamadhanDbContext _db;
        private readonly ServiceAssignmentService _sas;
        private readonly NotificationService _ns;
        private readonly IEmailService _emailService;

        public CustomerController(
            LifeSamadhanDbContext db,
            ServiceAssignmentService sas,
            NotificationService ns,
            IEmailService emailService)
        {
            _db = db;
            _sas = sas;
            _ns = ns;
            _emailService = emailService;
        }

        private long CurrentUserId()
        {
            var idClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(idClaim)) return 0;
            return long.Parse(idClaim);
        }

        [HttpGet("requests")]
        public IActionResult MyRequests()
        {
            var uid = CurrentUserId();
            var list = _db.ServiceRequests
                .Where(r => r.CustomerId == uid)
                .OrderByDescending(r => r.CreatedAt)
                .ToList();

            
            foreach (var r in list) r.OTP = "PROTECTED";

            return Ok(list);
        }

        [HttpGet("assignments")]
        public IActionResult MyAssignments()
        {
            var uid = CurrentUserId();
            var reqIds = _db.ServiceRequests
                .Where(r => r.CustomerId == uid)
                .Select(r => r.Id)
                .ToList();

            
            var assignments = _db.ServiceAssignments
                .Where(a => reqIds.Contains(a.RequestId))
                .OrderByDescending(a => a.AssignedAt)
                .ToList();

            var result = assignments.Select(a => {
                a.Otp = "PROTECTED"; 
                var req = _db.ServiceRequests.Find(a.RequestId);
                if (req != null) req.OTP = "PROTECTED"; 
                
                var pProfile = _db.ServiceProviders.Find(a.ProviderId);
                var pUser = pProfile != null ? _db.Users.Find(pProfile.UserId) : null;
                
                var svc = _db.Services.Find(req?.ServiceId ?? 0);
                var cat = _db.ServiceCategories.Find(req?.ServiceCategoryId ?? 0);

                return new
                {
                    assignment = a,
                    provider = new { 
                        userName = pUser?.Name ?? "Assigned Provider",
                        id = a.ProviderId
                    },
                    request = req,
                    service = svc != null ? new { name = svc.Name } : (cat != null ? new { name = cat.Name } : new { name = "General Service" }),
                    rating = _db.Ratings.FirstOrDefault(r => r.ServiceRequestId == a.RequestId)
                };
            }).ToList();

            return Ok(result);
        }

        [HttpGet("payments")]
        public IActionResult MyPayments()
        {
            var uid = CurrentUserId();
            var list = _db.Payments
                .Where(p => p.CustomerId == uid)
                .OrderByDescending(p => p.CreatedAt)
                .ToList();

            return Ok(list);
        }

        [HttpGet("profile")]
        public IActionResult GetProfile()
        {
            var uid = CurrentUserId();
            Console.WriteLine($"[DEBUG] Getting profile for UID: {uid}");
            
            var user = _db.Users.Find(uid);
            if (user == null) return NotFound("User not found");

            var profile = _db.CustomerProfiles.FirstOrDefault(p => p.UserId == uid);
            if (profile != null) 
                Console.WriteLine($"[DEBUG] Found profile: Address={profile.Address}, LocationId={profile.LocationId}");
            else
                Console.WriteLine("[DEBUG] No profile found for this user");
            
            var location = (profile != null && profile.LocationId > 0) 
                              ? _db.Locations.Find(profile.LocationId) 
                              : null;

            return Ok(new { user, profile, location });
        }

        [HttpPut("profile")]
        public IActionResult UpdateProfile([FromBody] CustomerProfileUpdateDTO updated)
        {
            var uid = CurrentUserId();
            Console.WriteLine($"[DEBUG] Updating profile for UID: {uid}. Name={updated.Name}, Mobile={updated.Mobile}, Address={updated.Address}, LocationId={updated.LocationId}");
            
            var user = _db.Users.Find(uid);
            if (user == null) return NotFound("User not found");

            
            if (!string.IsNullOrEmpty(updated.Name)) user.Name = updated.Name;
            if (!string.IsNullOrEmpty(updated.Mobile)) user.Mobile = updated.Mobile;

            try 
            {
                
                var profile = _db.CustomerProfiles.FirstOrDefault(p => p.UserId == uid);
                if (profile == null)
                {
                    Console.WriteLine("[DEBUG] Creating new CustomerProfile during update");
                    profile = new CustomerProfile { UserId = uid };
                    _db.CustomerProfiles.Add(profile);
                }
                
                if (!string.IsNullOrEmpty(updated.Address)) profile.Address = updated.Address;
                
                if (updated.LocationId.HasValue && updated.LocationId > 0) 
                {
                    profile.LocationId = updated.LocationId.Value;
                }

                _db.SaveChanges();
                Console.WriteLine("[DEBUG] Profile updated in DB");
                
                
                var location = (profile.LocationId > 0) ? _db.Locations.Find(profile.LocationId) : null;
                return Ok(new { user, profile, location, message = "Profile updated successfully" });
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[ERROR] UpdateProfile Failed: {ex.Message}");
                if (ex.InnerException != null) Console.WriteLine($"[ERROR] Inner: {ex.InnerException.Message}");
                return StatusCode(500, new { error = "Failed to update profile. " + ex.Message });
            }
        }

        
        
        

        [HttpPost("request")]
        public async Task<IActionResult> SubmitRequest([FromBody] CreateServiceRequestDTO dto)
        {
            Console.WriteLine($"[DEBUG] DTO Received: Category={dto.ServiceCategoryId}, Service={dto.ServiceId}, Location={dto.LocationId}, Provider={dto.ProviderId}, Amount={dto.Amount}");
            
            var uid = CurrentUserId();
            if (uid == 0) return Unauthorized();

            
            bool hasService = dto.ServiceId.HasValue && dto.ServiceId.Value > 0;
            bool hasCategory = dto.ServiceCategoryId.HasValue && dto.ServiceCategoryId.Value > 0;
            
            if (!hasService && !hasCategory)
            {
                return BadRequest(new { error = "Either ServiceId or ServiceCategoryId must be provided" });
            }

            
            var locationExits = await _db.Locations.AnyAsync(l => l.Id == dto.LocationId);
            if (!locationExits)
            {
                return BadRequest(new { error = $"Location with ID {dto.LocationId} does not exist." });
            }

            try
            {
                Console.WriteLine("[DEBUG] Step 1: Creating Request Entity");
                
                DateTime scheduledDate;
                if (!DateTime.TryParse(dto.ScheduledDate, out scheduledDate))
                {
                    scheduledDate = DateTime.Now.AddDays(1); 
                    Console.WriteLine($"[WARNING] Could not parse date '{dto.ScheduledDate}', using fallback.");
                }

                
                var request = new ServiceRequest
                {
                    CustomerId = uid,
                    ServiceId = dto.ServiceId,
                    ServiceCategoryId = dto.ServiceCategoryId,
                    LocationId = dto.LocationId,
                    ProviderId = dto.ProviderId,
                    ServiceAddress = dto.ServiceAddress ?? "",
                    ScheduledDate = scheduledDate,
                    PaymentStatus = dto.PaymentStatus ?? "PENDING",
                    Amount = dto.Amount,
                    Status = "REQUESTED",
                    OTP = new Random().Next(1000, 9999).ToString(),
                    CreatedAt = DateTime.UtcNow
                };
                
                Console.WriteLine($"[DEBUG] Step 2 & 3: Adding Request and Assignment to Context");
                _db.ServiceRequests.Add(request);
                
                
                if (dto.ProviderId.HasValue && dto.ProviderId.Value > 0)
                {
                    
                    var assignment = new ServiceAssignment
                    {
                        RequestId = 0, 
                        
                        
                        
                        ProviderId = dto.ProviderId.Value,
                        Status = "ASSIGNED",
                        AssignedAt = DateTime.UtcNow,
                        Otp = request.OTP 
                    };
                    
                    
                    
                    
                    await _db.SaveChangesAsync(); 
                    assignment.RequestId = request.Id; 
                    _db.ServiceAssignments.Add(assignment);
                    await _db.SaveChangesAsync(); 
                    
                    Console.WriteLine($"[DEBUG] Request {request.Id} and Assignment {assignment.Id} saved.");

                    
                    
                    try 
                    {
                        Console.WriteLine("[DEBUG] Step 6: Notifying Provider and Sending OTP Email");
                        var provider = await _db.ServiceProviders.FindAsync(dto.ProviderId.Value);
                        if (provider != null) 
                        {
                            await _ns.Send(provider.UserId, "You have a new service request!");
                            Console.WriteLine($"[DEBUG] Notification sent to UserId: {provider.UserId}");
                        }

                        
                        var customer = await _db.Users.FindAsync(uid);
                        if (customer != null && !string.IsNullOrEmpty(customer.Email))
                        {
                            string subject = "Your LifeSamadhan Service OTP";
                            string body = $@"
                                <html>
                                <body style='font-family: Arial, sans-serif;'>
                                    <h2 style='color: #2c3e50;'>LifeSamadhan Service Verification</h2>
                                    <p>Hello {customer.Name},</p>
                                    <p>Your service request has been booked successfully!</p>
                                    <p style='font-size: 1.2em;'>Your One-Time Password (OTP) for verification is: <b>{request.OTP}</b></p>
                                    <p>Please share this OTP with your service provider when they arrive at your location to start the service.</p>
                                    <br/>
                                    <p>Best Regards,<br/>LifeSamadhan Team</p>
                                </body>
                                </html>";
                            await _emailService.SendEmailAsync(customer.Email, subject, body);
                            Console.WriteLine($"[DEBUG] OTP Email sent to: {customer.Email}");
                        }
                    }
                    catch (Exception ex)
                    {
                         
                         Console.WriteLine($"[WARNING] Notification/Email failed: {ex.Message}");
                    }
                    
                    return Ok(new { 
                        success = true,
                        requestId = request.Id,
                        
                        message = "Service booked successfully! Check your email for the OTP."
                    });
                }

                
                return Ok(new { 
                    success = true,
                    requestId = request.Id,
                    message = "Request created but no provider selected" 
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[ERROR] Request CRASHED at logic step.");
                Console.WriteLine($"[ERROR] Message: {ex.Message}");
                if (ex.InnerException != null) 
                {
                     Console.WriteLine($"[ERROR] Inner: {ex.InnerException.Message}");
                     Console.WriteLine($"[ERROR] Inner Stack: {ex.InnerException.StackTrace}");
                }
                Console.WriteLine($"[ERROR] Stack: {ex.StackTrace}");
                return StatusCode(500, new { error = ex.Message, inner = ex.InnerException?.Message });
            }
        }

        [HttpPost("cancel/{assignmentId}")]
        public IActionResult CancelRequest(long assignmentId)
        {
            var assignment = _db.ServiceAssignments.Find(assignmentId);
            if (assignment == null)
                return NotFound("Assignment not found");

            var req = _db.ServiceRequests.Find(assignment.RequestId);
            if (req == null) return NotFound("Request not found");
            var uid = CurrentUserId();
            if (req.CustomerId != uid) return Forbid();

            if (assignment.Status == "CANCELLED_BY_CUSTOMER")
                return BadRequest("This request is already cancelled");

            if (assignment.Status == "STARTED" || assignment.Status == "COMPLETED")
                return BadRequest("Service already started – cancellation not allowed");

            assignment.Status = "CANCELLED_BY_CUSTOMER";
            assignment.CompletedAt = DateTime.Now;
            
            req.Status = "CANCELLED"; 

            _db.SaveChanges();

            return Ok(new
            {
                message = "Service request cancelled successfully",
                assignment
            });
        }

        [HttpPost("rating/{id}")]
        public IActionResult SubmitRating(long id, [FromBody] Rating r)
        {
            Console.WriteLine($"[DEBUG] SubmitRating called for ID: {id}");
            
            
            var req = _db.ServiceRequests.Find(id);
            if (req == null)
            {
                var assignment = _db.ServiceAssignments.Find(id);
                if (assignment != null)
                {
                    req = _db.ServiceRequests.Find(assignment.RequestId);
                }
            }

            if (req == null) return NotFound("Service Request or Assignment not found");
            
            var uid = CurrentUserId();
            

            
            
            bool isCompleted = req.Status == "COMPLETED" || req.Status == "PAID" || req.Status == "STARTED";
            if (!isCompleted)
            {
                isCompleted = _db.ServiceAssignments.Any(a => a.RequestId == req.Id && (a.Status == "COMPLETED" || a.Status == "STARTED"));
            }

            if (!isCompleted)
            {
                Console.WriteLine($"[DEBUG] Rating denied for Request {req.Id}. Status={req.Status}");
                return BadRequest("Rating allowed only after service has started or completed");
            }

            
            var existing = _db.Ratings.FirstOrDefault(rt => rt.ServiceRequestId == req.Id);
            if (existing != null)
                return BadRequest("You have already rated this service");
            
            
            long? providerTableId = req.ProviderId;
            if (!providerTableId.HasValue || providerTableId == 0)
            {
                providerTableId = _db.ServiceAssignments
                    .Where(a => a.RequestId == req.Id)
                    .Select(a => a.ProviderId)
                    .FirstOrDefault();
            }

            if (!providerTableId.HasValue || providerTableId == 0)
                return BadRequest("No provider linked to this request");

            var provider = _db.ServiceProviders.Find(providerTableId.Value);
            if (provider == null) return BadRequest("Provider profile not found");

            
            r.ServiceRequestId = req.Id;
            r.ReviewerId = uid;
            r.RevieweeId = provider.UserId; 
            r.CreatedAt = DateTime.Now;

            _db.Ratings.Add(r);
            _db.SaveChanges();

            return Ok(r);
        }
    }
}
