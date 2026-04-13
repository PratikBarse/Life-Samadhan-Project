using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LifeSamadhan.API.Models
{
    [Table("services")]
    public class Service
    {
        [Key]
        [Column("Id")]
        public long Id { get; set; }

        [Required(ErrorMessage = "CategoryId is required")]
        [Column("CategoryId")]
        public long CategoryId { get; set; }

        [ForeignKey(nameof(CategoryId))]
        public ServiceCategory? Category { get; set; }

        [Required(ErrorMessage = "Service name is required")]
        [StringLength(150, MinimumLength = 3,
            ErrorMessage = "Service name must be between 3 and 150 characters")]
        [Column("Name")]
        public string Name { get; set; } = string.Empty;

        [RegularExpression("^(ACTIVE|INACTIVE)$",
            ErrorMessage = "Status must be ACTIVE or INACTIVE")]
        [Column("Status")]
        public string Status { get; set; } = "ACTIVE";
    }
}
