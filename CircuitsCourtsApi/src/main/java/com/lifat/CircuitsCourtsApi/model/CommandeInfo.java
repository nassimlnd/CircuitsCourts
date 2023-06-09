package com.lifat.CircuitsCourtsApi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.lifat.CircuitsCourtsApi.service.CommandeDetailService;
import com.lifat.CircuitsCourtsApi.service.CommandeProducteurService;
import com.lifat.CircuitsCourtsApi.service.CommandeService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Cette classe sert simplement d'enveloppe pour envoyer le plus d'info possibles dans une seule requete http, ne se stock pas dans la BD.
 * Une CommandeInfo contient une commande et toutes les commandes détails associées à la commande
 *
 * Une Commande info est utilisée dans 2 cas :
 *  -on à besoin de tous les details d'une commande(Admin ou organisateur) et on recupère toutes les données d'une commande.
 *  -on à besoin des détails d'un commande en fonction d'un producteur(producteur) on recupere les données de cette commande en fonction
 *  de ce producteur.
 */
@Data
@NoArgsConstructor
public class CommandeInfo {
    @JsonProperty("commande")
    private Commande commande;

    //premet de ne pas metre le contenu dans le json envoyé dans la requete fasterxml.jacksonAnnotation
    @JsonIgnore
    private CommandeDetailService commandeDetailService;

    @JsonIgnore
    private CommandeProducteurService commandeProducteurService;

    @JsonProperty("commandesDetails")
    private Collection<CommandeDetail> commandesDetails;

    @JsonProperty("commandesProducteur")
    private Collection<CommandeProducteur> commandesProducteur;

    /**
     * l'injection de dependence @Autowired ne marche pas pour les classes non gérées par spring il faut faire une injection classique
     * @param commande
     * @param commandeDetailService
     * @param commandeProducteurService
     */
    public CommandeInfo(Commande commande, CommandeDetailService commandeDetailService,
                        CommandeProducteurService commandeProducteurService){
        this.commande = commande;
        this.commandeDetailService = commandeDetailService;
        this.commandeProducteurService = commandeProducteurService;
        this.commandesDetails = new ArrayList<>();
        this.commandesProducteur = new ArrayList<>();
    }

    /**
     * remplit la collection de CommandeDetails avec les commandes details du producteur
     * remplit la collection de CommandesProducteur avec les bonnes CommandesProducteur
     * @param idProducteur
     */
    public void fillWithCommandeDetailsAndCommandeProducteurByPRodAndCommande(Long idProducteur){
           Iterable<CommandeDetail> temp = (commandeDetailService.findAllByCommandeAndProducteur(idProducteur, this.commande.getId()));
        for (CommandeDetail cd: temp) {
            commandesDetails.add(cd);
        }
        for (CommandeDetail cd: commandesDetails) {
            commandesProducteur.add(commandeProducteurService.getCommandeProdByCommandeDetailAndProducteur(cd.getId(), idProducteur));
        }
    }

}
