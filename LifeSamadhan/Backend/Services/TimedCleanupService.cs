using LifeSamadhan.API.Data;
using Microsoft.EntityFrameworkCore;

namespace LifeSamadhan.API.Services
{
    public class TimedCleanupService : BackgroundService
    {
        private readonly IServiceProvider _serviceProvider;
        private readonly ILogger<TimedCleanupService> _logger;

        public TimedCleanupService(IServiceProvider serviceProvider, ILogger<TimedCleanupService> logger)
        {
            _serviceProvider = serviceProvider;
            _logger = logger;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("Timed Cleanup Service is starting.");

            while (!stoppingToken.IsCancellationRequested)
            {
                try
                {
                    await DoWork(stoppingToken);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error occurred in TimedCleanupService.");
                }

                
                await Task.Delay(TimeSpan.FromMinutes(2), stoppingToken);
            }
        }

        private async Task DoWork(CancellationToken stoppingToken)
        {
            using (var scope = _serviceProvider.CreateScope())
            {
                var db = scope.ServiceProvider.GetRequiredService<LifeSamadhanDbContext>();

                var now = DateTime.UtcNow;
                var acceptanceTimeoutLimit = now.AddMinutes(-30);
                var otpTimeoutLimit = now.AddMinutes(-30);

                
                var pendingAcceptance = await db.ServiceAssignments
                    .Where(a => a.Status == "ASSIGNED" && a.AssignedAt < acceptanceTimeoutLimit)
                    .ToListAsync(stoppingToken);

                foreach (var assignment in pendingAcceptance)
                {
                    assignment.Status = "TIMEOUT";
                    assignment.RespondedAt = now;
                    
                    var request = await db.ServiceRequests.FindAsync(new object[] { assignment.RequestId }, stoppingToken);
                    if (request != null && (request.Status == "REQUESTED" || request.Status == "ASSIGNED"))
                    {
                        request.Status = "CANCELLED";
                        _logger.LogInformation($"Assignment {assignment.Id} and Request {request.Id} TIMEOUT (Not Accepted in 30 mins)");
                    }
                }

                
                var pendingStart = await db.ServiceAssignments
                    .Where(a => a.Status == "ACCEPTED" && a.AcceptedAt.HasValue && a.AcceptedAt.Value < otpTimeoutLimit)
                    .ToListAsync(stoppingToken);

                foreach (var assignment in pendingStart)
                {
                    assignment.Status = "TIMEOUT";
                    
                    var request = await db.ServiceRequests.FindAsync(new object[] { assignment.RequestId }, stoppingToken);
                    if (request != null && request.Status == "ACCEPTED")
                    {
                        request.Status = "CANCELLED";
                        _logger.LogInformation($"Assignment {assignment.Id} and Request {request.Id} TIMEOUT (OTP Not Entered in 30 mins)");
                    }
                }

                if (pendingAcceptance.Any() || pendingStart.Any())
                {
                    await db.SaveChangesAsync(stoppingToken);
                }
            }
        }
    }
}
