using Microsoft.AspNetCore.Mvc;
using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using System;

namespace LifeSamadhan.API.Controllers
{
    [ApiController]
    [Route("api/provider/location")]
    [Produces("application/json")]
    public class ProviderLocationController : ControllerBase
    {
        private readonly LifeSamadhanDbContext _db;

        public ProviderLocationController(LifeSamadhanDbContext db)
        {
            _db = db;
        }

      
        [HttpPost("add")]
        [ProducesResponseType(typeof(ProviderLocation), 200)]
        [ProducesResponseType(400)]
        public IActionResult AddLocation([FromBody] ProviderLocation location)
        {
            location.Status = "PENDING";  
            location.EffectiveFrom = DateTime.UtcNow;

            _db.ProviderLocations.Add(location);
            _db.SaveChanges();

            return Ok(location);
        }
    }
}
