using System.ComponentModel.DataAnnotations;

namespace LifeSamadhan.API.Models
{
    public class LoginRequestDTO
    {
        [Required(ErrorMessage = "Email is required")]
        [EmailAddress(ErrorMessage = "Invalid email format")]
        public string Email { get; set; } = string.Empty;

        [Required(ErrorMessage = "Password is required")]
        [StringLength(20, MinimumLength = 6,
            ErrorMessage = "Password must be between 6 and 20 characters")]
        public string Password { get; set; } = string.Empty;
    }
}
