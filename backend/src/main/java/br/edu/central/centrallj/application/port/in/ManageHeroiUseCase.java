package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.CreateHeroCommand;
import br.edu.central.centrallj.application.model.HeroDetailView;
import br.edu.central.centrallj.application.model.HeroView;
import br.edu.central.centrallj.application.model.MissionView;
import br.edu.central.centrallj.application.model.PatchHeroAvailabilityCommand;
import java.util.List;
import java.util.UUID;

public interface ManageHeroiUseCase {
  HeroView create(CreateHeroCommand command);

  List<HeroView> listAll();

  HeroDetailView getDetail(UUID id);

  List<MissionView> listMissionsForHero(UUID heroiId);

  HeroView patchAvailability(UUID id, PatchHeroAvailabilityCommand command);
}
