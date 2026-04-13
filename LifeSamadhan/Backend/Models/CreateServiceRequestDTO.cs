namespace LifeSamadhan.API.Models
{
    
    public class CreateServiceRequestDTO
    {
        public long? ServiceId { get; set; }
        public long? ServiceCategoryId { get; set; }
        public long LocationId { get; set; }
        public long? ProviderId { get; set; }
        public string ServiceAddress { get; set; } = string.Empty;
        public string ScheduledDate { get; set; }
        public string PaymentStatus { get; set; } = "PENDING";
        public decimal Amount { get; set; }
    }
}
