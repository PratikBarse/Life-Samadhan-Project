using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using LifeSamadhan.API.Services;
using System.Linq;


using ServiceProviderModel = LifeSamadhan.API.Models.ServiceProvider;

namespace LifeSamadhan.API.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/auth")]
    [Produces("application/json")]
    public class AuthController : ControllerBase
    {
        private readonly LifeSamadhanDbContext db;
        private readonly JwtService jwt;
        private readonly PasswordService ps;

        public AuthController(
            LifeSamadhanDbContext db,
            JwtService jwt,
            PasswordService ps)
        {
            this.db = db;
            this.jwt = jwt;
            this.ps = ps;
        }

        [AllowAnonymous]
        [HttpPost("register")]
        public IActionResult Register([FromBody] RegisterRequestDTO request)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            
            var existingUser = db.Users.FirstOrDefault(u => u.Email == request.Email);
            if (existingUser != null)
                return BadRequest("Email already registered");

            
            var user = new User
            {
                Name = request.Name,
                Email = request.Email,
                Mobile = request.Mobile,
                Password = ps.Hash(request.Password),
                Role = request.Role.ToUpper(),
                Status = "ACTIVE",
                CreatedAt = DateTime.UtcNow
            };

            db.Users.Add(user);
            db.SaveChanges();

            
            if (user.Role == "CUSTOMER")
            {
                var profile = new CustomerProfile 
                { 
                    UserId = user.Id,
                    Address = request.Address ?? string.Empty,
                    LocationId = request.LocationId ?? 0
                };
                db.CustomerProfiles.Add(profile);
                db.SaveChanges();
            }
            else if (user.Role == "SERVICEPROVIDER")
            {
                var provider = new ServiceProviderModel
                {
                    UserId = user.Id,
                    Verified = false,
                    Availability = "AVAILABLE",
                    ProviderType = request.ProviderType,
                    HourlyRate = request.HourlyRate,
                    ServiceCategoryId = request.ServiceCategoryId
                };

                
                if (request.LocationId.HasValue && request.LocationId > 0)
                {
                    var loc = db.Locations.Find(request.LocationId.Value);
                    if (loc != null)
                    {
                        provider.City = loc.District;
                        provider.State = loc.State;

                        
                        var provLoc = new ProviderLocation
                        {
                             LocationId = loc.Id,
                             Status = "APPROVED", 
                             EffectiveFrom = DateTime.UtcNow
                        };
                        provider.Locations.Add(provLoc);
                    }
                }
                else
                {
                    
                    provider.City = request.City;
                    provider.State = request.State;
                }

                db.ServiceProviders.Add(provider);
                db.SaveChanges();

                
                if (!string.IsNullOrWhiteSpace(request.Skills))
                {
                    var skillIds = request.Skills.Split(',')
                        .Select(s => s.Trim())
                        .Where(s => !string.IsNullOrEmpty(s))
                        .Select(s => long.TryParse(s, out var id) ? id : 0)
                        .Where(id => id > 0)
                        .ToList();

                    foreach (var serviceId in skillIds)
                    {
                        
                        var service = db.Services.Find(serviceId);
                        if (service != null)
                        {
                            var skill = new ProviderSkill
                            {
                                ProviderId = provider.Id,
                                ServiceId = serviceId,
                                Status = "PENDING",
                                Remarks = "Pending admin approval"
                            };
                            db.ProviderSkills.Add(skill);
                        }
                    }
                    db.SaveChanges();
                }
            }

            return Ok(new { 
                message = "Registration successful", 
                userId = user.Id,
                email = user.Email,
                role = user.Role
            });
        }

        [AllowAnonymous]
        [HttpPost("login")]
        public IActionResult Login([FromBody] LoginRequestDTO request)
        {
            try 
            {
                if (!ModelState.IsValid)
                    return BadRequest(ModelState);

                var u = db.Users.FirstOrDefault(x => x.Email == request.Email);
                if (u == null || !ps.Verify(request.Password, u.Password))
                    return Unauthorized("Invalid email or password");

                var token = jwt.Generate(u);

                return Ok(new { 
                    token = token, 
                    role = u.Role,
                    name = u.Name,
                    email = u.Email
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Backend Error: " + ex.Message, stackTrace = ex.StackTrace });
            }
        }
    }
}
