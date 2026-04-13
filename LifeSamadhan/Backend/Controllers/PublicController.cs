using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Linq;
using System;
using Microsoft.EntityFrameworkCore;

namespace LifeSamadhan.API.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/public")]
    public class PublicController : ControllerBase
    {
        private readonly LifeSamadhanDbContext _db;

        public PublicController(LifeSamadhanDbContext db)
        {
            _db = db;
        }

        [AllowAnonymous]
        [HttpGet("providers/search")]
        public IActionResult SearchProviders([FromQuery] long serviceId = 0, [FromQuery] string city = null, [FromQuery] long locationId = 0, [FromQuery] long categoryId = 0)
        {
             
             
            var query = _db.ServiceProviders.AsQueryable();

            
            query = query.Where(p => p.Verified == true && p.Availability == "AVAILABLE");

            
            if (categoryId > 0)
            {
                query = query.Where(p => p.ServiceCategoryId == categoryId);
            }

            
            if (serviceId > 0)
            {
                var providerIdsWithService = _db.ProviderSkills
                    .Where(ps => ps.ServiceId == serviceId && ps.Status == "APPROVED")
                    .Select(ps => ps.ProviderId);
                
                query = query.Where(p => providerIdsWithService.Contains(p.Id));
            }

            
            if (locationId > 0)
            {
                 var providerIdsInLocation = _db.ProviderLocations
                    .Where(pl => pl.LocationId == locationId && pl.Status == "APPROVED")
                    .Select(pl => pl.ProviderId);
                
                query = query.Where(p => providerIdsInLocation.Contains(p.Id));
            }
            else if (!string.IsNullOrEmpty(city))
            {
                var providerIdsInLocation = _db.ProviderLocations
                    .Include(pl => pl.Location)
                    .Where(pl => pl.Location.District.Contains(city) && pl.Status == "APPROVED")
                    .Select(pl => pl.ProviderId);

                query = query.Where(p => providerIdsInLocation.Contains(p.Id));
            }

            var providers = query.ToList();

            
            var result = providers.Select(p => {
                var stats = _db.Ratings
                    .Where(r => r.RevieweeId == p.UserId)
                    .GroupBy(r => 1)
                    .Select(g => new {
                        Avg = g.Average(r => (double)r.Stars),
                        Count = g.Count()
                    })
                    .FirstOrDefault();

                return new
                {
                    ProviderId = p.Id,
                    UserId = p.UserId,
                    Name = _db.Users.Find(p.UserId)?.Name ?? "Unknown",
                    HourlyRate = p.HourlyRate,
                    ProviderType = p.ProviderType,
                    Rating = Math.Round(stats?.Avg ?? 0, 1),
                    ReviewCount = stats?.Count ?? 0,
                    City = _db.ProviderLocations
                            .Where(pl => pl.ProviderId == p.Id)
                            .Select(pl => pl.Location.District)
                            .FirstOrDefault()
                };
            }).OrderByDescending(x => x.Rating).ToList();

            return Ok(result);
        }

        [AllowAnonymous]
        [HttpGet("services")]
        public IActionResult GetAllServices()
        {
            var services = _db.Services.Where(s => s.Status == "ACTIVE").ToList();
            return Ok(services);
        }

        [AllowAnonymous]
        [HttpGet("locations")]
        public IActionResult GetLocations()
        {
            var locs = _db.Locations.ToList(); 
            return Ok(locs);
        }

        [AllowAnonymous]
        [HttpGet("locations/active")]
        public IActionResult GetActiveLocations()
        {
            
            var activeLocationIds = _db.ProviderLocations
                .Include(pl => pl.Provider)
                .Where(pl => pl.Status == "APPROVED" && pl.Provider.Verified)
                .Select(pl => pl.LocationId)
                .Distinct()
                .ToList();

            var locations = _db.Locations
                .Where(l => activeLocationIds.Contains(l.Id))
                .ToList();

            return Ok(locations);
        }
    }
}
