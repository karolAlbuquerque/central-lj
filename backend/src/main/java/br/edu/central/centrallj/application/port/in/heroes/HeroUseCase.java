package br.edu.central.centrallj.application.port.in.heroes;

import br.edu.central.centrallj.dto.CreateHeroRequest;
import br.edu.central.centrallj.dto.HeroDetailResponse;
import br.edu.central.centrallj.dto.HeroResponse;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.dto.PatchHeroAvailabilityRequest;
import java.util.List;
import java.util.UUID;

/** Porta de entrada: operações de herói. */
public interface HeroUseCase {
  HeroResponse create(CreateHeroRequest request);

  List<HeroResponse> listAll();

  HeroDetailResponse getDetail(UUID id);

  List<MissionResponse> listMissionsForHero(UUID heroiId);

  HeroResponse patchAvailability(UUID id, PatchHeroAvailabilityRequest request);
}

