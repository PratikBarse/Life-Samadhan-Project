using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace LifeSamadhan.API.Controllers
{
    [Authorize(Roles = "ADMIN")]
    [ApiController]
    [Route("api/location")]
    public class LocationController : ControllerBase
    {
        private readonly LifeSamadhanDbContext db;

        public LocationController(LifeSamadhanDbContext db)
        {
            this.db = db;
        }

        [HttpGet]
        public IActionResult GetAll()
        {
            return Ok(db.Locations.ToList());
        }

        [HttpGet("{id:long}")]
        public IActionResult Get(long id)
        {
            var l = db.Locations.Find(id);
            if (l == null) return NotFound();

            return Ok(l);
        }

        [HttpPost]
        public IActionResult Create(Location l)
        {
            l.Status = "ACTIVE";
            db.Locations.Add(l);
            db.SaveChanges();
            return Ok(l);
        }

        [HttpPut("{id:long}")]
        public IActionResult Update(long id, Location l)
        {
            var existing = db.Locations.Find(id);
            if (existing == null) return NotFound();

            existing.Country = l.Country;
            existing.State = l.State;
            existing.District = l.District;
            existing.Pincode = l.Pincode;
            existing.Status = l.Status;

            db.SaveChanges();
            return Ok(existing);
        }

        [HttpDelete("{id:long}")]
        public IActionResult Delete(long id)
        {
            var l = db.Locations.Find(id);
            if (l == null) return NotFound();

            db.Locations.Remove(l);
            db.SaveChanges();
            return Ok();
        }
        [HttpGet("countries")]
        public IActionResult GetCountries()
        {
            var list = db.Locations
                .Select(x => x.Country)
                .Distinct()
                .OrderBy(x => x)
                .ToList();

            return Ok(list);
        }
        [HttpGet("states/{country}")]
        public IActionResult GetStates(string country)
        {
            var list = db.Locations
                .Where(x => x.Country == country)
                .Select(x => x.State)
                .Distinct()
                .OrderBy(x => x)
                .ToList();

            return Ok(list);
        }
        [HttpGet("districts/{state}")]
        public IActionResult GetDistricts(string state)
        {
            var list = db.Locations
                .Where(x => x.State == state)
                .Select(x => x.District)
                .Distinct()
                .OrderBy(x => x)
                .ToList();

            return Ok(list);
        }
        [HttpGet("pincodes/{district}")]
        public IActionResult GetPincodes(string district)
        {
            var list = db.Locations
                .Where(x => x.District == district)
                .Select(x => x.Pincode)
                .Distinct()
                .OrderBy(x => x)
                .ToList();

            return Ok(list);
        }
        
    }
}
