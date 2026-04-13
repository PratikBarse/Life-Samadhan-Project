
using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LifeSamadhan.API.Models
{
    [Table("servicerequests")]
    public class ServiceRequest
    {
        [Key]
        [Column("Id")]
        public long Id { get; set; }

        [Column("CustomerId")]
        public long CustomerId { get; set; }

        [Column("ServiceId")]
        public long? ServiceId { get; set; }

        [Column("ServiceCategoryId")]
        public long? ServiceCategoryId { get; set; }

        [Column("LocationId")]
        public long LocationId { get; set; }

        [Column("ProviderId")]
        public long? ProviderId { get; set; }

        [Column("ServiceAddress")]
        public string ServiceAddress { get; set; } = string.Empty;

        [Column("ScheduledDate")]
        public DateTime ScheduledDate { get; set; }

        [Column("Status")]
        public string Status { get; set; } = "REQUESTED";

        [Column("OTP")]
        public string? OTP { get; set; } 

        [Column("CompletionDate")]
        public DateTime? CompletionDate { get; set; }

        [Column("PaymentStatus")]
        public string PaymentStatus { get; set; } = "PENDING";

        [Column("Amount")]
        public decimal Amount { get; set; }

        [Column("CreatedAt")]
        public DateTime CreatedAt { get; set; } = DateTime.Now;

        
        [ForeignKey("CustomerId")]
        public User? Customer { get; set; }

        [ForeignKey("ServiceId")]
        public Service? Service { get; set; }

        [ForeignKey("ServiceCategoryId")]
        public ServiceCategory? ServiceCategory { get; set; }

        [ForeignKey("ProviderId")]
        public ServiceProvider? Provider { get; set; }

        [ForeignKey("LocationId")]
        public Location? Location { get; set; }
    }
}