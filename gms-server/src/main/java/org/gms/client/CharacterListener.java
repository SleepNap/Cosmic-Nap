package org.gms.client;

import org.gms.tools.PacketCreator;
import org.gms.tools.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CharacterListener implements AbstractCharacterListener {
    private final Character character;
    public CharacterListener(Character character) {
        this.character = character;
    }

    @Override
    public void onHpChanged(int oldHp) {
        character.hpChangeAction(oldHp);
    }

    @Override
    public void onHpmpPoolUpdate() {
        List<Pair<Stat, Integer>> hpmpupdate = character.recalcLocalStats();
        for (Pair<Stat, Integer> p : hpmpupdate) {
            character.statUpdates.put(p.getLeft(), p.getRight());
        }

        if (character.hp > character.localmaxhp) {
            character.setHp(character.localmaxhp);
            character.statUpdates.put(Stat.HP, character.hp);
        }

        if (character.mp > character.localmaxmp) {
            character.setMp(character.localmaxmp);
            character.statUpdates.put(Stat.MP, character.mp);
        }
    }

    @Override
    public void onStatUpdate() {
        character.recalcLocalStats();
    }

    @Override
    public void onAnnounceStatPoolUpdate() {
        List<Pair<Stat, Integer>> statup = new ArrayList<>(8);
        for (Map.Entry<Stat, Integer> s : character.statUpdates.entrySet()) {
            statup.add(new Pair<>(s.getKey(), s.getValue()));
        }

        character.sendPacket(PacketCreator.updatePlayerStats(statup, true, character));
    }
}
