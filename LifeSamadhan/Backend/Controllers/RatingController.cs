using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Linq;
using System;
using System.Security.Claims;

namespace LifeSamadhan.API.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/rating")]
    public class RatingController : ControllerBase
    {
        private readonly LifeSamadhanDbContext _db;

        public RatingController(LifeSamadhanDbContext db)
        {
            _db = db;
        }

        private long CurrentUserId()
        {
            var idClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(idClaim)) return 0;
            return long.Parse(idClaim);
        }

        [HttpPost("submit")]
        public IActionResult SubmitRating([FromBody] Rating r)
        {
            var uid = CurrentUserId();
            if (uid == 0) return Unauthorized();

            var req = _db.ServiceRequests.Find(r.ServiceRequestId);

            if (req == null)
                return NotFound("Invalid Service Request");

            if (req.Status != "COMPLETED" && req.Status != "PAID")
                return BadRequest("Rating allowed only after service completion");

            
            var alreadyRated = _db.Ratings
                .Any(x => x.ServiceRequestId == r.ServiceRequestId && x.ReviewerId == uid);

            if (alreadyRated)
                return BadRequest("Rating already submitted for this service");

            r.ReviewerId = uid;
            
            
            
            
            if (uid == req.CustomerId)
            {
                if (!req.ProviderId.HasValue) return BadRequest("No provider to rate");
                r.RevieweeId = req.ProviderId.Value;
            }
            else if (uid == req.ProviderId) 
            {
                
                
                
                var provider = _db.ServiceProviders.Find(req.ProviderId);
                if (provider == null || provider.UserId != uid) return Forbid();
                
                r.RevieweeId = req.CustomerId; 
                
            }
            else
            {
                 return Forbid("You are not part of this service request");
            }
            
            r.CreatedAt = DateTime.Now;

            _db.Ratings.Add(r);
            _db.SaveChanges();

            return Ok(r);
        }
    }
}
