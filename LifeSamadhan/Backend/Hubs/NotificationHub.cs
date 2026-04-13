using Microsoft.AspNetCore.SignalR;
using System.Threading.Tasks;

namespace LifeSamadhan.API.Hubs
{
    public class NotificationHub : Hub
    {
        
        public override async Task OnConnectedAsync()
        {
            var userId = Context.UserIdentifier;
            Console.WriteLine($"[SignalR] User connected: {userId}");
            await base.OnConnectedAsync();
        }

        public async Task SendNotification(string userId, string message)
        {
            await Clients.User(userId).SendAsync("ReceiveNotification", message);
        }

        
        
    }
}
