using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Linq;

namespace LifeSamadhan.API.Controllers
{
    [ApiController]
    [Route("api/providertype")]
    [Produces("application/json")]
    public class ProviderTypeController : ControllerBase
    {
        private readonly LifeSamadhanDbContext db;

        public ProviderTypeController(LifeSamadhanDbContext db)
        {
            this.db = db;
        }

        
        [AllowAnonymous]
        [HttpGet("active")]
        public IActionResult GetActive()
        {
            var activeTypes = db.ProviderTypes
                .Where(pt => pt.Status == "ACTIVE")
                .OrderBy(pt => pt.Name)
                .ToList();

            return Ok(activeTypes);
        }

        
        [Authorize(Roles = "ADMIN")]
        [HttpGet]
        public IActionResult GetAll()
        {
            var types = db.ProviderTypes
                .OrderBy(pt => pt.Name)
                .ToList();

            return Ok(types);
        }

        [Authorize(Roles = "ADMIN")]
        [HttpGet("{id}")]
        public IActionResult Get(long id)
        {
            var type = db.ProviderTypes.Find(id);
            if (type == null) 
                return NotFound($"Provider type with ID {id} not found");

            return Ok(type);
        }

        [Authorize(Roles = "ADMIN")]
        [HttpPost]
        public IActionResult Create([FromBody] ProviderType providerType)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            
            var existing = db.ProviderTypes
                .FirstOrDefault(pt => pt.Name.ToLower() == providerType.Name.ToLower());
            
            if (existing != null)
                return BadRequest("Provider type with this name already exists");

            providerType.Status = "ACTIVE";
            providerType.CreatedAt = DateTime.Now;

            db.ProviderTypes.Add(providerType);
            db.SaveChanges();

            return Ok(providerType);
        }

        [Authorize(Roles = "ADMIN")]
        [HttpPut("{id}")]
        public IActionResult Update(long id, [FromBody] ProviderType updatedType)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var existing = db.ProviderTypes.Find(id);
            if (existing == null) 
                return NotFound($"Provider type with ID {id} not found");

            
            var duplicate = db.ProviderTypes
                .FirstOrDefault(pt => pt.Name.ToLower() == updatedType.Name.ToLower() && pt.Id != id);
            
            if (duplicate != null)
                return BadRequest("Provider type with this name already exists");

            existing.Name = updatedType.Name;
            existing.Description = updatedType.Description;
            existing.Status = updatedType.Status;

            db.SaveChanges();

            return Ok(existing);
        }

        [Authorize(Roles = "ADMIN")]
        [HttpDelete("{id}")]
        public IActionResult Delete(long id, [FromQuery] bool force = false)
        {
            var type = db.ProviderTypes.Find(id);
            if (type == null) 
                return NotFound($"Provider type with ID {id} not found");

            
            var providersUsingType = db.ServiceProviders
                .Where(sp => sp.ProviderType == type.Name)
                .ToList();

            if (providersUsingType.Count > 0)
            {
                if (!force)
                {
                    return BadRequest($"Cannot delete provider type. {providersUsingType.Count} service provider(s) are using this type. Set status to INACTIVE instead or use force=true to delete anyway.");
                }

                
                foreach (var provider in providersUsingType)
                {
                    provider.ProviderType = null;
                }
                db.SaveChanges();
            }

            db.ProviderTypes.Remove(type);
            db.SaveChanges();

            return Ok(new { 
                message = "Provider type deleted successfully",
                providersUpdated = providersUsingType.Count
            });
        }
    }
}
