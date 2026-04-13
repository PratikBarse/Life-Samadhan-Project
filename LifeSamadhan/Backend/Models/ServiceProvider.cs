using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LifeSamadhan.API.Models
{
    [Table("serviceproviders")]
    public class ServiceProvider
    {
        [Key]
        [Column("Id")]
        public long Id { get; set; }

        [Column("Verified")]
        public bool Verified { get; set; } = false;

        [Required(ErrorMessage = "Availability is required")]
        [RegularExpression("^(AVAILABLE|BUSY|OFFLINE)$",
            ErrorMessage = "Availability must be AVAILABLE, BUSY or OFFLINE")]
        [Column("Availability")]
        public string Availability { get; set; } = "AVAILABLE";

        [Required(ErrorMessage = "UserId is required")]
        [Column("UserId")]
        public long UserId { get; set; }

        [ForeignKey(nameof(UserId))]
        public User? User { get; set; }

        [Column("ProviderType")]
        public string? ProviderType { get; set; }

        [Column("HourlyRate")]
        public decimal? HourlyRate { get; set; }

        [Column("ServiceCategoryId")]
        public long? ServiceCategoryId { get; set; }

        [Column("City")]
        public string? City { get; set; }

        [Column("State")]
        public string? State { get; set; }

        [NotMapped]
        public int CompletedJobsCount { get; set; } = 0;

        [NotMapped]
        public int RejectedJobsCount { get; set; } = 0;

        [ForeignKey(nameof(ServiceCategoryId))]
        public ServiceCategory? ServiceCategory { get; set; }

        public List<ProviderLocation> Locations { get; set; } = new();

        public List<ProviderSkill> Skills { get; set; } = new();
    }
}
