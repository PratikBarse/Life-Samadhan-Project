

using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LifeSamadhan.API.Models
{
    [Table("providerskills")]
    public class ProviderSkill
    {
        [Key]
        [Column("Id")]
        public long Id { get; set; }

        [Required(ErrorMessage = "ProviderId is required")]
        [Column("ProviderId")]
        public long ProviderId { get; set; }

        [Required(ErrorMessage = "ServiceId is required")]
        [Column("ServiceId")]
        public long ServiceId { get; set; }

        [Required(ErrorMessage = "Status is required")]
        [RegularExpression(
            "^(PENDING|APPROVED|REJECTED|INACTIVE)$",
            ErrorMessage = "Status must be PENDING, APPROVED, REJECTED or INACTIVE"
        )]
        [Column("Status")]
        public string Status { get; set; } = "PENDING";

        [StringLength(250, ErrorMessage = "Remarks cannot exceed 250 characters")]
        [Column("Remarks")]
        public string? Remarks { get; set; }

        [ForeignKey(nameof(ProviderId))]
        public ServiceProvider? Provider { get; set; }

        [ForeignKey(nameof(ServiceId))]
        public Service? Service { get; set; }
    }
}
