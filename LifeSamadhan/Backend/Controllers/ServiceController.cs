using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Linq;

namespace LifeSamadhan.API.Controllers
{
    [Authorize(Roles ="ADMIN")]
    [ApiController]
    [Route("api/service")]
    public class ServiceController : ControllerBase
    {
        private readonly LifeSamadhanDbContext db;

        public ServiceController(LifeSamadhanDbContext db)
        {
            this.db = db;
        }

        [AllowAnonymous]
        [HttpGet]
        public IActionResult GetAll()
        {
            var services = db.Services.ToList();
            return Ok(services);
        }

     
        [AllowAnonymous]
        [HttpGet("{id:long}")]
        public IActionResult Get(long id)
        {
            var s = db.Services.Find(id);
            if (s == null) return NotFound();

            return Ok(s);
        }

        [AllowAnonymous]
        [HttpGet("by-category/{categoryId:long}")]
        public IActionResult GetByCategory(long categoryId)
        {
            var list = db.Services
                .Where(x => x.CategoryId == categoryId)
                .ToList();

            return Ok(list);
        }

        [HttpPost]
        public IActionResult Create(Service s)
        {
            s.Status = "ACTIVE";
            db.Services.Add(s);
            db.SaveChanges();

            return Ok(s);
        }

   
        [HttpPut("{id:long}")]
        public IActionResult Update(long id, Service s)
        {
            var existing = db.Services.Find(id);
            if (existing == null) return NotFound();

            existing.Name = s.Name;
            existing.CategoryId = s.CategoryId;
            existing.Status = s.Status;

            db.SaveChanges();
            return Ok(existing);
        }

        [HttpDelete("{id:long}")]
        public IActionResult Delete(long id)
        {
            var s = db.Services.Find(id);
            if (s == null) return NotFound();

            db.Services.Remove(s);
            db.SaveChanges();

            return Ok();
        }
    }
}
