using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LifeSamadhan.API.Models
{
    [Table("providerlocations")]
    public class ProviderLocation
    {
        [Key]
        [Column("Id")]
        public long Id { get; set; }

        [Required(ErrorMessage = "ProviderId is required")]
        [Column("ProviderId")]
        public long ProviderId { get; set; }

        [Required(ErrorMessage = "LocationId is required")]
        [Column("LocationId")]
        public long LocationId { get; set; }

        [Required(ErrorMessage = "Status is required")]
        [RegularExpression("^(PENDING|ACTIVE|REJECTED)$",
            ErrorMessage = "Status must be PENDING, ACTIVE or REJECTED")]
        [Column("Status")]
        public string Status { get; set; } = "PENDING";

        [Required(ErrorMessage = "EffectiveFrom date is required")]
        [Column("EffectiveFrom")]
        public DateTime EffectiveFrom { get; set; } = DateTime.UtcNow;

        [Column("EffectiveTo")]
        public DateTime? EffectiveTo { get; set; }

        [ForeignKey(nameof(ProviderId))]
        public ServiceProvider? Provider { get; set; }

        [ForeignKey(nameof(LocationId))]
        public Location? Location { get; set; }
    }
}
