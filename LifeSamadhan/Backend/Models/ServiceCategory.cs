using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LifeSamadhan.API.Models
{
    [Table("servicecategories")]
    public class ServiceCategory
    {
        [Key]
        [Column("Id")]
        public long Id { get; set; }

        [Required(ErrorMessage = "Category name is required")]
        [StringLength(100, MinimumLength = 3,
            ErrorMessage = "Category name must be between 3 and 100 characters")]
        [Column("Name")]
        public string Name { get; set; } = string.Empty;

        [RegularExpression("^(ACTIVE|INACTIVE)$",
            ErrorMessage = "Status must be ACTIVE or INACTIVE")]
        [Column("Status")]
        public string Status { get; set; } = "ACTIVE";
    }
}