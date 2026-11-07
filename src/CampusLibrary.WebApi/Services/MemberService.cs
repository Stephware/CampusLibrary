using CampusLibrary.Data.Entities;
using CampusLibrary.Data.Repositories.Interfaces;
using CampusLibrary.WebApi.DTOs;
using CampusLibrary.WebApi.Services.Interfaces;

namespace CampusLibrary.WebApi.Services;

public class MemberService : IMemberService
{
    private readonly IMemberRepository _repository;
    private readonly ILogger<MemberService> _logger;

    public MemberService(IMemberRepository repository, ILogger<MemberService> logger)
    {
        _repository = repository;
        _logger = logger;
    }

    public async Task<List<MemberDto>> GetAllMembersAsync() =>
        (await _repository.GetAllAsync()).Select(ToDto).ToList();

    public async Task<MemberDto?> GetMemberByIdAsync(int id)
    {
        var member = await _repository.GetByIdAsync(id);
        return member == null ? null : ToDto(member);
    }

    public async Task<MemberDto> CreateMemberAsync(CreateMemberDto dto)
    {
        var email = dto.Email.Trim();
        var studentId = dto.StudentId.Trim();
        if (await _repository.ExistsByEmailOrStudentIdAsync(email, studentId))
            throw new InvalidOperationException("A member with this email or student ID already exists.");

        var member = await _repository.AddAsync(new Member
        {
            Name = dto.Name.Trim(), Email = email, StudentId = studentId,
            RegisteredDate = DateTime.UtcNow
        });
        _logger.LogInformation("Created member with ID {Id}", member.Id);
        return ToDto(member);
    }

    public async Task UpdateMemberAsync(int id, UpdateMemberDto dto)
    {
        var member = await _repository.GetByIdAsync(id)
            ?? throw new KeyNotFoundException("Member not found.");
        var email = dto.Email.Trim();
        var studentId = dto.StudentId.Trim();
        if (await _repository.ExistsByEmailOrStudentIdAsync(email, studentId, id))
            throw new InvalidOperationException("A member with this email or student ID already exists.");

        member.Name = dto.Name.Trim();
        member.Email = email;
        member.StudentId = studentId;
        await _repository.UpdateAsync(member);
        _logger.LogInformation("Updated member with ID {Id}", id);
    }

    public async Task DeleteMemberAsync(int id)
    {
        var member = await _repository.GetByIdAsync(id)
            ?? throw new KeyNotFoundException("Member not found.");
        if (await _repository.HasLoansAsync(id))
            throw new InvalidOperationException("A member with loan history cannot be deleted.");

        await _repository.DeleteAsync(member);
        _logger.LogInformation("Deleted member with ID {Id}", id);
    }

    private static MemberDto ToDto(Member member) => new()
    {
        Id = member.Id, Name = member.Name, Email = member.Email,
        StudentId = member.StudentId,
        RegisteredDate = DateTime.SpecifyKind(member.RegisteredDate, DateTimeKind.Utc)
    };
}
