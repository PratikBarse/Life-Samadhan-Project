using System;

namespace LifeSamadhan.API.Models
{
    public class Notification
    {
        public long Id { get; set; }
        public long UserId { get; set; }
        public string Message { get; set; } = "";
        public DateTime CreatedAt { get; set; } = DateTime.Now;
    }
}
