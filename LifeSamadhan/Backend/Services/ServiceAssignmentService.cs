using System;
using System.Linq;
using System.Threading.Tasks;
using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;

namespace LifeSamadhan.API.Services
{
    public class ServiceAssignmentService
    {
        private readonly LifeSamadhanDbContext _db;
        private const int OTP_MIN = 100000;
        private const int OTP_MAX = 999999;

        private readonly NotificationService _notificationService;
        private readonly IEmailService _emailService;

        public ServiceAssignmentService(
            LifeSamadhanDbContext db, 
            NotificationService notificationService,
            IEmailService emailService)
        {
            _db = db;
            _notificationService = notificationService;
            _emailService = emailService;
        }

        
        
        
        public async Task<ServiceAssignment> AssignProvider(long requestId)
        {
            var request = _db.ServiceRequests.Find(requestId);
            if (request == null) return null;

            var eligibleProviders =
                _db.ServiceProviders

                
                .Join(
                    _db.ProviderLocations,
                    p => p.Id,
                    pl => pl.ProviderId,
                    (p, pl) => new { p, pl }
                )

                
                .Join(
                    _db.ProviderSkills,
                    x => x.p.Id,
                    ps => ps.ProviderId,
                    (x, ps) => new { x.p, x.pl, ps }
                )

                .Where(x =>
                    x.pl.LocationId == request.LocationId &&
                    x.pl.Status == "APPROVED" && 
                    
                    
                    x.pl.EffectiveFrom <= DateTime.UtcNow &&
                    (x.pl.EffectiveTo == null || x.pl.EffectiveTo >= DateTime.UtcNow) &&

                    x.ps.ServiceId == request.ServiceId &&
                    x.ps.Status == "APPROVED" &&

                    x.p.Verified == true &&
                    x.p.Availability == "AVAILABLE"
                )

                .Select(x => x.p)
                .Distinct()
                .ToList();

            if (!eligibleProviders.Any())
                return null;

            
            var bestProvider = eligibleProviders
                .Select(p => new 
                { 
                    Provider = p, 
                    Rating = _db.Ratings.Where(r => r.RevieweeId == p.Id).Average(r => (double?)r.Stars) ?? 0 
                })
                .OrderByDescending(x => x.Rating)
                .First()
                .Provider;

            var assignment = new ServiceAssignment
            {
                RequestId = request.Id,
                ProviderId = bestProvider.Id,
                Otp = new Random().Next(OTP_MIN, OTP_MAX).ToString(),
                AssignedAt = DateTime.UtcNow,
                Status = "ASSIGNED"
            };

            _db.ServiceAssignments.Add(assignment);
            await _db.SaveChangesAsync(); 

            
            await _notificationService.Send(bestProvider.UserId, $"New Service Request Assigned! Request #{request.Id}");

            
            try
            {
                var customer = await _db.Users.FindAsync(request.CustomerId);
                if (customer != null && !string.IsNullOrEmpty(customer.Email))
                {
                    string subject = "Your LifeSamadhan Service OTP";
                    string body = $@"
                        <html>
                        <body style='font-family: Arial, sans-serif;'>
                            <h2 style='color: #2c3e50;'>LifeSamadhan Service Verification</h2>
                            <p>Hello {customer.Name},</p>
                            <p>A service provider has been assigned to your request!</p>
                            <p style='font-size: 1.2em;'>Your One-Time Password (OTP) for verification is: <b>{assignment.Otp}</b></p>
                            <p>Please share this OTP with your service provider when they arrive to start the service.</p>
                            <br/>
                            <p>Best Regards,<br/>LifeSamadhan Team</p>
                        </body>
                        </html>";
                    await _emailService.SendEmailAsync(customer.Email, subject, body);
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[WARNING] Auto-Assignment OTP Email failed: {ex.Message}");
            }

            return assignment;
        }
    }
}
