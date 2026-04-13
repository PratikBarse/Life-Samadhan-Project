using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Linq;

namespace LifeSamadhan.API.Controllers
{
    [Authorize(Roles ="ADMIN")]
    [ApiController]
    [Route("api/category")]
    public class ServiceCategoryController : ControllerBase
    {
        private readonly LifeSamadhanDbContext db;

        public ServiceCategoryController(LifeSamadhanDbContext db)
        {
            this.db = db;
        }

        [AllowAnonymous]
        [HttpGet]
        public IActionResult GetAll()
        {
            var list = db.ServiceCategories
                .Where(x => x.Status == "ACTIVE")
                .ToList();
            return Ok(list);
        }

        [HttpGet("{id}")]
        public IActionResult Get(long id)
        {
            var sc = db.ServiceCategories.Find(id);
            if (sc == null) return NotFound();

            return Ok(sc);
        }

        [HttpPost]
        public IActionResult Create(ServiceCategory c)
        {
            if (db.ServiceCategories.Any(x => x.Name == c.Name))
            {
                return BadRequest("Category with this name already exists");
            }

            c.Status = "ACTIVE"; 
            db.ServiceCategories.Add(c);
            db.SaveChanges();

            return Ok(c);
        }

        [HttpPut("{id}")]
        public IActionResult Update(long id, ServiceCategory c)
        {
            var existing = db.ServiceCategories.Find(id);
            if (existing == null) return NotFound();

            if (db.ServiceCategories.Any(x => x.Name == c.Name && x.Id != id))
            {
                return BadRequest("Category with this name already exists");
            }

            existing.Name = c.Name;
            existing.Status = c.Status;

            db.SaveChanges();
            return Ok(existing);
        }

        [HttpDelete("{id}")]
        public IActionResult Delete(long id)
        {
            var sc = db.ServiceCategories.Find(id);
            if (sc == null) return NotFound();

            
            bool hasProviders = db.ServiceProviders.Any(sp => sp.ServiceCategoryId == id);
            bool hasServices = db.Services.Any(s => s.CategoryId == id);

            if (hasProviders || hasServices)
            {
                
                sc.Status = "INACTIVE";
                db.SaveChanges();
                return Ok(new { message = "Category marked as INACTIVE due to existing dependencies." });
            }

            
            db.ServiceCategories.Remove(sc);
            db.SaveChanges();

            return Ok(new { message = "Category permanent deleted." });
        }
    }
}
