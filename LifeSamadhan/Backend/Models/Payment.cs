using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LifeSamadhan.API.Models
{
    [Table("Payments")]
    public class Payment
    {
        [Key]
        [Column("Id")]
        public long Id { get; set; }

        [Column("AssignmentId")]
        public long AssignmentId { get; set; }
        
        [Column("CustomerId")]
        public long CustomerId { get; set; }
        
        [Column("ProviderId")]
        public long ProviderId { get; set; }

        [Column("amount")]
        public double Amount { get; set; }

        [Column("PaymentStatus")]
        public string PaymentStatus { get; set; } = "PENDING";

        [Column("payment_status")]
        public string? PaymentStatusExtra { get; set; } = "PENDING"; 

        [Column("RazorpayOrderId")]
        public string? RazorpayOrderId { get; set; }
        
        [Column("RazorpayPaymentId")]
        public string? RazorpayPaymentId { get; set; }
        
        [Column("RazorpaySignature")]
        public string? RazorpaySignature { get; set; }

        [Column("CreatedAt")]
        public DateTime CreatedAt { get; set; } = DateTime.Now;
        
        [Column("PaidAt")]
        public DateTime? PaidAt { get; set; }
    }
}
