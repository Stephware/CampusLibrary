using System.ComponentModel.DataAnnotations;

namespace CampusLibrary.WebApi.DTOs;

public class MemberDto
{
    public int Id { get; set; }
    public required string Name { get; set; }
    public required string Email { get; set; }
    public required string StudentId { get; set; }
    public DateTime RegisteredDate { get; set; }
}

public class CreateMemberDto
{
    [Required, MaxLength(100)]
    public required string Name { get; set; }

    [Required, EmailAddress, MaxLength(100)]
    public required string Email { get; set; }

    [Required, MaxLength(20)]
    public required string StudentId { get; set; }
}

public class UpdateMemberDto : CreateMemberDto { }
