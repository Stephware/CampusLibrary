using CampusLibrary.Data.Entities;

namespace CampusLibrary.Data.Repositories;

public record BookAvailability(Book Book, bool IsAvailable);
