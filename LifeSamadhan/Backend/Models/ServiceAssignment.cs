
using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LifeSamadhan.API.Models
{
    [Table("serviceassignments")]
    public class ServiceAssignment
    {
        [Key]
        [Column("Id")]
        public long Id { get; set; }

        [Required(ErrorMessage = "RequestId is required")]
        [Column("RequestId")]
        public long RequestId { get; set; }

        [Required(ErrorMessage = "ProviderId is required")]
        [Column("ProviderId")]
        public long ProviderId { get; set; }

        [Required(ErrorMessage = "Status is required")]
        [Column("Status")]
        public string Status { get; set; } = "ASSIGNED";

        [Required(ErrorMessage = "OTP is required")]
        [Column("Otp")]
        public string Otp { get; set; } = string.Empty;

        [Column("AssignedAt")]
        public DateTime AssignedAt { get; set; } = DateTime.UtcNow;

        [Column("RespondedAt")]
        public DateTime? RespondedAt { get; set; }

        [Column("AcceptedAt")]
        public DateTime? AcceptedAt { get; set; }

        [Column("StartedAt")]
        public DateTime? StartedAt { get; set; }

        [Column("CompletedAt")]
        public DateTime? CompletedAt { get; set; }
    }
}
