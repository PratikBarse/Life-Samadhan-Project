using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LifeSamadhan.API.Models
{
    [Table("locations")]
    public class Location
    {
        [Key]
        [Column("Id")]
        public long Id { get; set; }

        [Required(ErrorMessage = "Country is required")]
        [StringLength(50, ErrorMessage = "Country name is too long")]
        [Column("Country")]
        public string Country { get; set; } = "India";

        [Required(ErrorMessage = "State is required")]
        [StringLength(100, ErrorMessage = "State name is too long")]
        [Column("State")]
        public string State { get; set; } = string.Empty;

        [Required(ErrorMessage = "District is required")]
        [StringLength(100, ErrorMessage = "District name is too long")]
        [Column("District")]
        public string District { get; set; } = string.Empty;

        [Required(ErrorMessage = "Pincode is required")]
        [RegularExpression(@"^[1-9][0-9]{5}$",
            ErrorMessage = "Invalid Indian pincode")]
        [Column("Pincode")]
        public string Pincode { get; set; } = string.Empty;

        [RegularExpression("^(ACTIVE|INACTIVE)$",
            ErrorMessage = "Status must be ACTIVE or INACTIVE")]
        [Column("Status")]
        public string Status { get; set; } = "ACTIVE";
    }
}
