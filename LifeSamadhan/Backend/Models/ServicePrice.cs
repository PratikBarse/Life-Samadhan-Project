using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LifeSamadhan.API.Models
{
    public class ServicePrice
    {
        [Key]
        public long Id { get; set; }

        [Required(ErrorMessage = "ServiceId is required")]
        [Range(1, long.MaxValue, ErrorMessage = "ServiceId must be a valid value")]
        public long ServiceId { get; set; }

        [ForeignKey(nameof(ServiceId))]
        public Service? Service { get; set; }

        [Required(ErrorMessage = "Price is required")]
        [Range(1, double.MaxValue, ErrorMessage = "Price must be greater than 0")]
        [Column(TypeName = "decimal(18,2)")]
        public decimal Price { get; set; }

        [Required(ErrorMessage = "EffectiveFrom date is required")]
        public DateTime EffectiveFrom { get; set; } = DateTime.Today;

        public DateTime? EffectiveTo { get; set; }
    }
}
