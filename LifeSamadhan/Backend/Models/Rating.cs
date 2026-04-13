using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LifeSamadhan.API.Models
{
    [Table("ratings")]
    public class Rating
    {
        [Key]
        [Column("Id")]
        public long Id { get; set; }

        [Required]
        [Column("ServiceRequestId")]
        public long ServiceRequestId { get; set; }

        [Required]
        [Column("ReviewerId")]
        public long ReviewerId { get; set; } 

        [Required]
        [Column("RevieweeId")]
        public long RevieweeId { get; set; } 

        [Range(1, 5)]
        [Column("Stars")]
        public int Stars { get; set; }

        [Column("Comment")]
        public string Comment { get; set; } = string.Empty;

        [Column("CreatedAt")]
        public DateTime CreatedAt { get; set; } = DateTime.Now;

        [Column("feedback")]
        public string? Feedback { get; set; }

        [ForeignKey("ServiceRequestId")]
        public ServiceRequest? ServiceRequest { get; set; }

        [ForeignKey("ReviewerId")]
        public User? Reviewer { get; set; }

        [ForeignKey("RevieweeId")]
        public User? Reviewee { get; set; }
    }
}
