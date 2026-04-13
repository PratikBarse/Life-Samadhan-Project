using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using Microsoft.AspNetCore.SignalR;
using LifeSamadhan.API.Hubs;
using System.Threading.Tasks;

namespace LifeSamadhan.API.Services
{
    public class NotificationService
    {
        private readonly LifeSamadhanDbContext db;
        private readonly IHubContext<NotificationHub> _hub;

        public NotificationService(LifeSamadhanDbContext db, IHubContext<NotificationHub> hub)
        {
            this.db = db;
            _hub = hub;
        }

        public async Task Send(long userId, string message)
        {
            var n = new Notification
            {
                UserId = userId,
                Message = message
            };

            db.Notifications.Add(n);
            await db.SaveChangesAsync();

            System.Console.WriteLine($"[SignalR] Sending notification to User {userId}: {message}");
            await _hub.Clients.User(userId.ToString()).SendAsync("ReceiveNotification", message);
        }
    }
}
