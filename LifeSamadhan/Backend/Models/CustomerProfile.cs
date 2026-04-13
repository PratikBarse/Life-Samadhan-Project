using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LifeSamadhan.API.Models
{
    [Table("customerprofiles")]
    public class CustomerProfile
    {
        [Key]
        [Column("Id")]
        public long Id { get; set; }

        [Column("LocationId")]
        public long LocationId { get; set; }

        [StringLength(500, ErrorMessage = "Address is too long")]
        [Column("Address")]
        public string Address { get; set; } = string.Empty;

        [Required(ErrorMessage = "UserId is required")]
        [Column("UserId")]
        public long UserId { get; set; }

        [ForeignKey(nameof(UserId))]
        public User? User { get; set; }
    }
}