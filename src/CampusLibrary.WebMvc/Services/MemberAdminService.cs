using CampusLibrary.Data.Entities;
using CampusLibrary.Data.Repositories.Interfaces;
using CampusLibrary.WebMvc.Services.Interfaces;
using CampusLibrary.WebMvc.ViewModels;

namespace CampusLibrary.WebMvc.Services;

public class MemberAdminService : IMemberAdminService
{
    private readonly IMemberRepository _repository;
    private readonly ILogger<MemberAdminService> _logger;

    public MemberAdminService(IMemberRepository repository, ILogger<MemberAdminService> logger)
    {
        _repository = repository;
        _logger = logger;
    }

    public async Task<List<MemberViewModel>> GetAllAsync() =>
        (await _repository.GetAllAsync()).OrderBy(m => m.Name).Select(m => new MemberViewModel
        {
            Id = m.Id, Name = m.Name, Email = m.Email, StudentId = m.StudentId,
            RegisteredDate = m.RegisteredDate
        }).ToList();

    public async Task CreateAsync(MemberFormViewModel model)
    {
        var email = model.Email.Trim();
        var studentId = model.StudentId.Trim();
        if (await _repository.ExistsByEmailOrStudentIdAsync(email, studentId))
            throw new InvalidOperationException("A member with this email or student ID already exists. Check the members list before adding another record.");
        var member = await _repository.AddAsync(new Member
        {
            Name = model.Name.Trim(), Email = email, StudentId = studentId,
            RegisteredDate = DateTime.UtcNow
        });
        _logger.LogInformation("Admin created member {Id}", member.Id);
    }
}
