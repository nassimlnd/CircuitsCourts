package com.lifat.CircuitsCourtsApi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.lifat.CircuitsCourtsApi.model.*;
import com.lifat.CircuitsCourtsApi.payload.response.OrderDetailsResponse;
import com.lifat.CircuitsCourtsApi.payload.response.OrderProductDetailsResponse;
import com.lifat.CircuitsCourtsApi.payload.response.OrderProductProducerResponse;
import com.lifat.CircuitsCourtsApi.service.*;
import com.sun.source.tree.TryTree;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

//@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
/**
 * end point de l'api pour les commandes.
 * L'api ayant évolué nous n'utilisons presque plus les méthodes sur les objets Commande, CommandeDetail, CommandeProducteur mais commandeInfo.
 * Pour tous les objets nous appliqons les focnctionnalitées CRUD donc les requetes ont le généralement le même but pour les différents objets.
 * Nous avons laissé les méthodes sur les commandes de base car elles peuvent être utilies dans de rares cas.
 */
public class CommandeController {
    @Autowired
    private CommandeService commandeService;
    @Autowired
    private CommandeDetailService commandeDetailService;
    @Autowired
    private CommandeProducteurService commandeProducteurService;

    @Autowired
    private ProducteurServices producteurServices;

    @Autowired
    private ProduitService produitService;

    /**
     * Commandes
     */
    @GetMapping("/commandes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    public ResponseEntity<?> getCommandes() {
        return ResponseEntity.ok(commandeService.getCommandes());
    }

    @GetMapping("/commandes/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    public ResponseEntity<?> getCommandeById(@PathVariable Long id) {

        return ResponseEntity.ok(commandeService.getCommande(id));
    }

    @PostMapping("/commandes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    public ResponseEntity<?> saveCommande(@RequestBody Commande commande) {
        Commande savedCommande = commandeService.saveCommande(commande);
        return ResponseEntity.ok(savedCommande);

    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    @ResponseBody
    @DeleteMapping("/commandes/{id}")
    public ResponseEntity<?> deletCommandeById(@PathVariable Long id) throws Exception {
        if (commandeService.getCommande(id).isEmpty()) {
            throw new Exception("commande n°" + id + " does not exist");
        }
        commandeService.deleteCommande(id);
        //envoi d'une reponse status 204 pour indiquer que la ressource à été supprimée avec succes.
        return ResponseEntity.noContent().build();
    }


    /**
     * Obtenir tous les détails de commandes.
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    @GetMapping("/commandes/details")
    public ResponseEntity<?> getAllCommandeDetails() {
        return ResponseEntity.ok(commandeDetailService.getCommandeDetails());
    }


    /**
     * Obtenir une commandeDetail avec son idCommande.
     *
     * @param id
     * @return
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    @GetMapping("/commandes/{id}/details")
    public ResponseEntity<?> getCommandeDetailByIdCommande(@PathVariable Long id) {
        return ResponseEntity.ok(commandeDetailService.getCommandeDetailByCommandeId(id));
    }

    /**
     * Obtenir une CommandeDetail avec son id_CommandeDetail
     *
     * @param id
     * @return CommandeDetail
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    @GetMapping("/commandes/details/{id}")
    public ResponseEntity<?> getCommandeDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(commandeDetailService.getCommandeDetail(id));
    }


    /**
     * Sauvegarder un détail de commande.
     *
     * @param commandeDetail
     * @return la CommandeDetail
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    @PostMapping("/commandes/details")
    public ResponseEntity<?> saveCommandeDetail(@RequestBody CommandeDetail commandeDetail) {
        return ResponseEntity.ok(commandeDetailService.saveCommandeDetail(commandeDetail));
    }


    /**
     * Supprimer un détail de commande.
     *
     * @param id
     * @return
     * @throws Exception si la CommandeDetail à supprimer n'existe pas
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANSIATEUR')")
    @ResponseBody
    @DeleteMapping("/commandes/details/{id}")
    public ResponseEntity<?> deleteCommandeDetail(@PathVariable Long id) throws Exception {
        if (commandeDetailService.getCommandeDetail(id) == null) {
            throw new Exception("commandeDetail n°" + id + " does not exist");
        }
        commandeDetailService.deleteCommandeDetail(id);
        //envoi d'une reponse status 204 pour indiquer que la ressource à été supprimée avec succes.
        return ResponseEntity.noContent().build();
    }


    /**
     * @return toutes les CommandeProducteur
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANSIATEUR')")
    @GetMapping("/commandes/producteurs")
    public ResponseEntity<?> getAllCommandeProducteur() {
        return ResponseEntity.ok(commandeProducteurService.getCommandeProducteurs());
    }

    /**
     * @param commandeProducteur
     * @return la commande producteur enregistrée
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    @PostMapping("/commandes/producteurs")
    public ResponseEntity<?> saveCommandeProducteur(@RequestBody CommandeProducteur commandeProducteur) {
        return ResponseEntity.ok(commandeProducteurService.saveCommandeProducteur(commandeProducteur));
    }

    /**
     * Supprime la commande producteur via son id
     *
     * @param id
     * @return un messige si la CommandeProducteur à bien été supprimé
     * @throws Exception si la CommandeProducteur n'existe pas
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    @ResponseBody
    @DeleteMapping("/commandes/producteurs/{id}")
    public ResponseEntity<?> deleteCommandeProducteur(@PathVariable Long id) throws Exception {
        if (commandeProducteurService.getCommandeProducteur(id) == null) {
            throw new Exception("commandeProducteur n°" + id + " does not exist");
        }
        commandeProducteurService.deleteCommandeProducteur(id);
        //envoi d'une reponse status 204 pour indiquer que la ressource à été supprimée avec succes.
        return ResponseEntity.noContent().build();
    }

    /**
     * @param id
     * @return Toutes les Commande d'un client
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    @GetMapping("/commandes/client/{id}")
    public ResponseEntity<?> getCommandesByClientId(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.getCommandesByClientId(id));
    }


    /**
     * Cherche les commandeInfo par producteur et toutes les commandes details et producteur associées
     *
     * @param id
     * @return les CommandeInfo d'un producteur.
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR') or hasRole('PRODUCTEUR')")
    @GetMapping("/commandesInfo/producteur/{id}")
    public ResponseEntity<?> getCommandesInfoByProducteurId(@PathVariable Long id) {
        //une commandeInfo est créée à partir d'une commande obligatoirement,si pas de commandes pas de commande info.
        Optional<Commande> existingCommande = commandeService.getCommande(id);
        if (existingCommande.isEmpty()) {
            return ResponseEntity.badRequest().body("la commande n°" + id + " n'existe pas");
        }
        //recupere toutes les commandes du producteur
        Collection<Commande> commandes = (Collection<Commande>) commandeService.getAllCommandesByProd(id);
        Collection<CommandeInfo> commandesInfo = new ArrayList<>();
        //pour chaque commandes recupere les commandesDetails du producteur associe a la commande et les commandeProducteur aussi
        for (Commande c : commandes) {
            CommandeInfo oneCommandeInfo = new CommandeInfo(c, commandeDetailService, commandeProducteurService);
            oneCommandeInfo.fillWithCommandeDetailsAndCommandeProducteurByPRodAndCommande(id);
            commandesInfo.add(oneCommandeInfo);
        }
        return ResponseEntity.ok(commandesInfo);
    }

    /**
     * Récupère une commandeInfo via l'id de sa Commande
     *
     * @param id
     * @return la COmmandeInfo constituée de sa Commande, ses COmmandesDetail et CommandeProducteur.
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    @GetMapping("/commandes/info/{id}")
    public ResponseEntity<?> getOrderInfoById(@PathVariable Long id) {
        if (!commandeService.getCommande(id).isPresent()) {
            return ResponseEntity.badRequest().body("la commande n°" + id + " n'existe pas");
        }

        Commande commande = commandeService.getCommande(id).get();
        Iterable<Produit> produits = produitService.getProductsByOrder(id);
        Iterable<CommandeDetail> commandeDetails = commandeDetailService.getCommandeDetailByCommandeId(id);

        ArrayList<OrderProductDetailsResponse> orderProductResponses = new ArrayList<>();


        for (CommandeDetail commandeDetail : commandeDetails) {
            ArrayList<OrderProductProducerResponse> orderProductProducerResponses = new ArrayList<>();
            if (!produitService.getProduit(commandeDetail.getIdProduit()).isPresent()) {
                return ResponseEntity.badRequest().body("le produit n°" + commandeDetail.getIdProduit() + " n'existe pas");
            }
            Produit produit = produitService.getProduit(commandeDetail.getIdProduit()).get();
            Iterable<CommandeProducteur> commandeProducteurs = commandeProducteurService.getCommandesProducteurByCommandeDetail(commandeDetail.getId());
            for (CommandeProducteur cp : commandeProducteurs) {
                Optional<Producteur> producteur = producteurServices.getProducteurById(cp.getIdProducteur());
                if (produit != null && producteur.isPresent()) {
                    Producteur producteur1 = producteur.get();
                    orderProductProducerResponses.add(new OrderProductProducerResponse(producteur1.getId_Producteur(), producteur1.getDescription(), producteur1.getTags(), producteur1.getLibelle(), producteur1.getAdresse(), producteur1.getMail(), producteur1.getRayon_Livraison(), cp.getQuantite()));
                }
            }

            orderProductResponses.add(new OrderProductDetailsResponse(produit.getId(), produit.getLibelle(), produit.getTva(), produit.getReference(), produit.getOrigineProduction(), produit.getOrigineTransformation(), produit.getAgriculture(), produit.getTypeProduit(), produit.getConditionnement(), produit.getDluo(), produit.getPrix(), commandeDetail.getQuantite(), orderProductProducerResponses));
        }

        OrderDetailsResponse orderDetailsResponse = new OrderDetailsResponse(commande.getId(), commande.getIdClient(), commande.getDateCommande(), orderProductResponses);


        return ResponseEntity.ok(orderDetailsResponse);
    }

    /**
     * Mise a jour totale de la commande, met le stock a jour
     *
     * @param id de la commande
     * @return la nouvelle commande
     */
    @PreAuthorize("hasRole('Admin') or hasRole ('ORGANISATEUR')")
    @PutMapping("/commande/update/{id}")
    public ResponseEntity<?> udateCommande(@PathVariable Long id, @RequestBody CommandeInfo commandeInfo) {
        try {
            //si la verification de l'update de la commande info est valide on enregistre tout dans la bd
            //la save d'un objet déja existant dans la bd == update.
            CommandeInfo commandeInfo1 = commandeService.verifCommandeInfoUpdate(commandeInfo);
            System.out.println(commandeInfo1.getCommandesProducteur().isEmpty());
            System.out.println(commandeInfo1.getCommandesDetails().isEmpty());
            //les verifications sont passées pour l'update
            commandeService.saveCommande(commandeInfo.getCommande());
            for (CommandeDetail cd : commandeInfo.getCommandesDetails()) {
                commandeDetailService.saveCommandeDetail(cd);
            }
            for (CommandeProducteur cp : commandeInfo.getCommandesProducteur()) {
                commandeProducteurService.saveCommandeProducteur(cp);
            }
            return ResponseEntity.ok().body(commandeInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /**
     * enregistre une nouvelle commande
     * met a jour le stock des producteur, leur enleve la quantite du produit demandé.
     *
     * @param commandeInfo : la commandeInfo à verifier et enregistrer dans la bd.
     * @return la nouvelle commande
     */
    @PreAuthorize("hasRole('Admin') or hasRole ('ORGANISATEUR')")
    @PostMapping("/commande/save")
    public ResponseEntity<?> saveCommande(@RequestBody CommandeInfo commandeInfo) throws Exception {
        //try {
        //on verifie que la commande n'existe pas deja

        commandeService.commandeExist(commandeInfo.getCommande().getId());
        if (commandeService.verifCommandeInfo(commandeInfo)) {
            commandeService.saveCommande(commandeInfo.getCommande());
            //enregistrement des commande details
            for (CommandeDetail cd : commandeInfo.getCommandesDetails()) {
                commandeDetailService.saveCommandeDetail(cd);
            }

            //enregistrement des commandes producteurs/update du stock
            for (CommandeProducteur cp : commandeInfo.getCommandesProducteur()) {
                commandeProducteurService.saveCommandeProducteur(cp);
                Optional<CommandeDetail> commandeDetail = commandeDetailService.getCommandeDetail(cp.getIdCommandeDetails());
                if (commandeDetail.isPresent()) {
                    producteurServices.updateQteProduit(cp.getIdProducteur(), commandeDetail.get().getIdProduit(), cp.getQuantite());
                }
            }

        }
        return ResponseEntity.ok().body(commandeInfo);


    }

    /**
     * Supprime par le biais d'une commande :
     * - la commande
     * - les commandes details associées
     * - les commandes producteurs associées aux commandes details
     * Met a jour le stock
     *
     * @param id id de la commande a supprimer
     * @return 204 si la ressource o ete supprime
     * @throws Exception si la commande n'existe pas dans la bd.
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANISATEUR')")
    @DeleteMapping("/commandesInfo/delete/{id}")
    public ResponseEntity<?> deletCommandeInfo(@PathVariable Long id) {
        try {
            CommandeInfo deletCommande = commandeService.getCommandeInfo(id);
            commandeService.deletCommandeInfo(deletCommande.getCommande().getId());
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        }
    }

    /**
     * Envoie les commandes liées à un produit d'id idProduit
     *
     * @param idProduit id du produit
     * @return les commandes liées à un produit d'id idProduit
     */
    @GetMapping("/commandes/produit/{idProduit}")
    public ResponseEntity<?> getAllCommandesByProduit(@PathVariable Long idProduit) {
        try {
            Iterable<Commande> commandes = commandeService.getAllCommandesByProduit(idProduit);
            return ResponseEntity.ok(commandes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}


