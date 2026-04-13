using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using System.Linq;
using System;


using ServiceProviderModel = LifeSamadhan.API.Models.ServiceProvider;

namespace LifeSamadhan.API.Controllers
{
    
    public class StatusUpdateRequest
    {
        public string Status { get; set; }
    }

    [Authorize(Roles = "ADMIN")]
    [ApiController]
    [Route("api/admin")]
    [Produces("application/json")]
    public class AdminController : ControllerBase
    {
        private readonly LifeSamadhanDbContext db;

        public AdminController(LifeSamadhanDbContext db)
        {
            this.db = db;
        }

       
        [HttpGet("users")]
        public IActionResult Users()
        {
            return Ok(db.Users.ToList());
        }

        
        [HttpGet("skills/pending")]
        public IActionResult PendingSkills()
        {
            var list = db.ProviderSkills
                .Include(s => s.Provider)
                    .ThenInclude(p => p.User)
                .Include(s => s.Service)
                .Where(s => s.Status == "PENDING")
                .Select(s => new {
                    s.Id,
                    s.ProviderId,
                    s.ServiceId,
                    s.Remarks,
                    s.Status,
                    Provider = s.Provider,
                    Service = s.Service
                })
                .ToList();

            return Ok(list);
        }

        [HttpPut("skill/{id}/approve")]
        public IActionResult ApproveSkill(long id)
        {
            var s = db.ProviderSkills.Find(id);
            if (s == null) return NotFound();

            s.Status = "APPROVED";
            db.SaveChanges();
            return Ok(s);
        }

       
        [HttpPut("skill/{id}/reject")]
        public IActionResult RejectSkill(long id)
        {
            var s = db.ProviderSkills.Find(id);
            if (s == null) return NotFound();

            s.Status = "REJECTED";
            db.SaveChanges();
            return Ok(s);
        }

        
        [HttpPut("location/{id}/approve")]
        public IActionResult ApproveLocation(long id)
        {
            var loc = db.ProviderLocations.Find(id);
            if (loc == null) return NotFound();

            loc.Status = "APPROVED";
            if (loc.EffectiveFrom == default)
                loc.EffectiveFrom = DateTime.UtcNow;

            db.SaveChanges();
            return Ok(loc);
        }

        
        [HttpPut("provider/{id}/verify")]
        public IActionResult VerifyProvider(long id)
        {
            var provider = db.ServiceProviders.Find(id);
            if (provider == null) return NotFound($"ServiceProvider not found: {id}");

            provider.Verified = true;
            db.SaveChanges();

            return Ok(provider);
        }

        
        [HttpPut("provider/{id}/status")]
        [ProducesResponseType(typeof(ServiceProviderModel), 200)]
        [ProducesResponseType(404)]
        [ProducesResponseType(400)]
        public IActionResult UpdateProviderStatus(long id, [FromBody] StatusUpdateRequest request)
        {
            var provider = db.ServiceProviders.Find(id);
            if (provider == null) return NotFound($"ServiceProvider not found: {id}");

            var user = db.Users.Find(provider.UserId);
            if (user == null) return NotFound($"User not found for provider: {id}");

            if (string.IsNullOrEmpty(request.Status) || 
                (request.Status != "ACTIVE" && request.Status != "INACTIVE"))
            {
                return BadRequest("Status must be either 'ACTIVE' or 'INACTIVE'");
            }

            user.Status = request.Status;
            db.SaveChanges();

            return Ok(new { 
                id = provider.Id,
                status = user.Status,
                message = $"Provider status updated to {user.Status}"
            });
        }

        
        [HttpPut("provider/{id}")]
        [ProducesResponseType(typeof(ServiceProviderModel), 200)]
        [ProducesResponseType(404)]
        public IActionResult UpdateProvider(long id, [FromBody] ServiceProviderModel updatedProvider)
        {
            var provider = db.ServiceProviders.Find(id);
            if (provider == null) return NotFound($"ServiceProvider not found: {id}");

            
            provider.Availability = updatedProvider.Availability ?? provider.Availability;
            provider.Verified = updatedProvider.Verified;

            db.SaveChanges();

            return Ok(provider);
        }

        [HttpDelete("provider/{id}")]
        public IActionResult DeleteProvider(long id)
        {
            var provider = db.ServiceProviders.Find(id);
            if (provider == null) return NotFound($"ServiceProvider not found: {id}");

            
            var user = db.Users.Find(provider.UserId);
            if (user != null)
            {
                db.Users.Remove(user);
            }

            db.ServiceProviders.Remove(provider);
            db.SaveChanges();

            return Ok(new { message = $"ServiceProvider with ID {id} and linked User deleted successfully" });
        }

        [HttpGet("providers")]
        public IActionResult Providers([FromQuery] bool? verified = null)
        {
            var providers = db.ServiceProviders.Include(p => p.User).AsQueryable();

            if (verified.HasValue)
            {
                providers = providers.Where(p => p.Verified == verified.Value);
            }

            return Ok(providers.ToList());
        }
    }
}
