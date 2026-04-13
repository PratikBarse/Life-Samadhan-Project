using System;
using System.ComponentModel.DataAnnotations;

namespace LifeSamadhan.API.Models
{
    public class ProviderType
    {
        [Key]
        public long Id { get; set; }

        [Required(ErrorMessage = "Provider type name is required")]
        [StringLength(100, MinimumLength = 2,
            ErrorMessage = "Provider type name must be between 2 and 100 characters")]
        public string Name { get; set; } = string.Empty;

        [StringLength(500, ErrorMessage = "Description cannot exceed 500 characters")]
        public string? Description { get; set; }

        [RegularExpression("^(ACTIVE|INACTIVE)$",
            ErrorMessage = "Status must be ACTIVE or INACTIVE")]
        public string Status { get; set; } = "ACTIVE";

        public DateTime CreatedAt { get; set; } = DateTime.Now;
    }
}
