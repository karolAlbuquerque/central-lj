package br.edu.central.centrallj.application.service;

import br.edu.central.centrallj.application.exception.ResourceNotFoundException;
import br.edu.central.centrallj.application.mapper.HeroApplicationMapper;
import br.edu.central.centrallj.application.mapper.MissionApplicationMapper;
import br.edu.central.centrallj.application.model.CreateHeroCommand;
import br.edu.central.centrallj.application.model.HeroDetailView;
import br.edu.central.centrallj.application.model.HeroView;
import br.edu.central.centrallj.application.model.MissionView;
import br.edu.central.centrallj.application.model.PatchHeroAvailabilityCommand;
import br.edu.central.centrallj.application.port.in.ManageHeroiUseCase;
import br.edu.central.centrallj.application.port.out.EquipePersistencePort;
import br.edu.central.centrallj.application.port.out.HeroiPersistencePort;
import br.edu.central.centrallj.application.port.out.MissionPersistencePort;
import br.edu.central.centrallj.domain.Heroi;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HeroiService implements ManageHeroiUseCase {

  private final HeroiPersistencePort heroiPersistencePort;
  private final EquipePersistencePort equipePersistencePort;
  private final MissionPersistencePort missionPersistencePort;
  private final HeroApplicationMapper heroMapper;
  private final MissionApplicationMapper missionMapper;

  public HeroiService(
      HeroiPersistencePort heroiPersistencePort,
      EquipePersistencePort equipePersistencePort,
      MissionPersistencePort missionPersistencePort,
      HeroApplicationMapper heroMapper,
      MissionApplicationMapper missionMapper) {
    this.heroiPersistencePort = heroiPersistencePort;
    this.equipePersistencePort = equipePersistencePort;
    this.missionPersistencePort = missionPersistencePort;
    this.heroMapper = heroMapper;
    this.missionMapper = missionMapper;
  }

  @Override
  @Transactional
  public HeroView create(CreateHeroCommand command) {
    Heroi h = new Heroi();
    h.setId(UUID.randomUUID());
    h.setNomeHeroico(command.nomeHeroico().trim());
    h.setNomeCivil(blankToNull(command.nomeCivil()));
    h.setEspecialidade(command.especialidade().trim());
    h.setStatusDisponibilidade(command.statusDisponibilidade());
    h.setNivel(command.nivel().trim());
    h.setAtivo(command.ativo());
    if (command.equipeId() != null) {
      h.setEquipe(
          equipePersistencePort
              .findById(command.equipeId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("Equipe não encontrada: " + command.equipeId())));
    }
    return heroMapper.toView(heroiPersistencePort.save(h));
  }

  @Override
  @Transactional(readOnly = true)
  public List<HeroView> listAll() {
    return heroiPersistencePort.findAllByOrderByNomeHeroicoAsc().stream()
        .map(heroMapper::toView)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<MissionView> listMissionsForHero(UUID heroiId) {
    if (!heroiPersistencePort.existsById(heroiId)) {
      throw new ResourceNotFoundException("Herói não encontrado: " + heroiId);
    }
    return missionPersistencePort.findByHeroiResponsavelIdOrderByDataCriacaoDesc(heroiId).stream()
        .map(missionMapper::toView)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public HeroDetailView getDetail(UUID id) {
    Heroi h =
        heroiPersistencePort
            .findByIdWithEquipe(id)
            .orElseThrow(() -> new ResourceNotFoundException("Herói não encontrado: " + id));
    var missoes =
        missionPersistencePort.findByHeroiResponsavelIdOrderByDataCriacaoDesc(id).stream()
            .map(missionMapper::toView)
            .toList();
    return new HeroDetailView(heroMapper.toView(h), missoes);
  }

  @Override
  @Transactional
  public HeroView patchAvailability(UUID id, PatchHeroAvailabilityCommand command) {
    Heroi h =
        heroiPersistencePort
            .findByIdWithEquipe(id)
            .orElseThrow(() -> new ResourceNotFoundException("Herói não encontrado: " + id));
    h.setStatusDisponibilidade(command.disponibilidade());
    return heroMapper.toView(heroiPersistencePort.save(h));
  }

  private static String blankToNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    return s.trim();
  }
}
