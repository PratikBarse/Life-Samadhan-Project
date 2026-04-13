using Microsoft.AspNetCore.Mvc;
using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using Microsoft.AspNetCore.Authorization;
using System.Linq;
using System.Security.Claims;





using ServiceProviderModel = LifeSamadhan.API.Models.ServiceProvider;

namespace LifeSamadhan.API.Controllers
{
    [Authorize(Roles = "SERVICEPROVIDER,ADMIN")]
    [ApiController]
    [Route("api/provider/skill")]
    [Produces("application/json")]
    public class ProviderSkillController : ControllerBase
    {
        private readonly LifeSamadhanDbContext _db;

        public ProviderSkillController(LifeSamadhanDbContext db)
        {
            _db = db;
        }

        private long CurrentUserId()
        {
            var idClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(idClaim)) return 0;
            return long.Parse(idClaim);
        }

        private ServiceProviderModel? GetProviderForCurrentUser()
        {
            var uid = CurrentUserId();
            if (uid == 0) return null;
            return _db.ServiceProviders.FirstOrDefault(p => p.UserId == uid);
        }

        [HttpPost("add")]
        [ProducesResponseType(typeof(ProviderSkill), 200)]
        [ProducesResponseType(400)]
        public IActionResult AddSkill([FromBody] ProviderSkillCreateDTO dto)
        {
            if (dto == null || dto.ServiceId <= 0)
                return BadRequest("ServiceId is required");

            var provider = GetProviderForCurrentUser();
            if (provider == null)
                return NotFound("Provider profile not found for current user");

            var service = _db.Services.Find(dto.ServiceId);
            if (service == null)
                return NotFound("Service not found");

            var skill = new ProviderSkill
            {
                ProviderId = provider.Id,
                ServiceId = dto.ServiceId,
                Remarks = dto.Remarks,
                Status = "PENDING"
            };

            _db.ProviderSkills.Add(skill);
            _db.SaveChanges();

            return Ok(skill);
        }
    }
}
