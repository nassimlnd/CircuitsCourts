package com.lifat.CircuitsCourtsApi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.lifat.CircuitsCourtsApi.model.*;
import com.lifat.CircuitsCourtsApi.repository.*;
import com.lifat.CircuitsCourtsApi.service.calculTournee.GeoPortailApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CommandeService {

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private ProducteurRepository producteurRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CommandeDetailService commandeDetailService;

    public Iterable<Commande> getCommandes() {
        return commandeRepository.findAll();
    }

    public Commande saveCommande(Commande commande) {
        Commande savedCommande = commandeRepository.save(commande);
        return savedCommande;
    }

    public Optional<Commande> getCommande(final Long id) {
        return commandeRepository.findById(id);
    }

    public void deleteCommande(final Long id) {
        commandeRepository.deleteById(id);
    }

    public Iterable<Commande> getCommandesByClientId(Long id) {
        return commandeRepository.findByIdClient(id);
    }


    public Iterable<Commande> getAllCommandesByProd(Long id) {
        return commandeRepository.findAllCommandesByProducteur(id);
    }


    @Autowired
    private ObjectMapper objectMapper;

    /***
     *  modifie la commande voulu en fonction du JsonPatch
     *  modification partielle de la commande
     * @param patch Json patch recupéré dans le controller
     * @param  targetCommande la commande ciblée
     * @return la nouvelle commande
     * @throws JsonPatchException
     * @throws JsonProcessingException
     */
    public Commande applyPatchToCommande(JsonPatch patch, Commande targetCommande) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetCommande, JsonNode.class));
        return objectMapper.treeToValue(patched, Commande.class);
    }

    // -------------------- verification de la commande --------------------

    /**
     * Les verifications sont utiles pour la création d'une commande
     * verifie l'existence du client, des producteurs et des produits
     * verifie pour chaque commandeProducteur si stock producteur >= commandeProducteur quantite
     * verifie que la quantite de la commandeDetail == somme des quantitees des commandesProd associees
     * verifie pour chaque producteur qu'il sont dans leurs rayon de livraison respectif
     *
     * @param commandeInfo
     * @return treu si et seulement si  aucunes verificatipns de la commandeInfo n'ont levees aucunes une exception
     */
    public boolean verifCommandeInfo(CommandeInfo commandeInfo) throws Exception {

        //verification de l'existence du client
        Commande newCommande = commandeInfo.getCommande();
        if (doesClientExist(newCommande.getIdClient())) {
            //verification de l'existence des produits

            HashMap<CommandeDetail, ArrayList<CommandeProducteur>> hashVerif = getCommandesProducteurByCommandeDetail(commandeInfo);
            for (Map.Entry mapEntry : hashVerif.entrySet()) {
                CommandeDetail cd = (CommandeDetail) mapEntry.getKey();
                doesCommandeDetailExist(cd.getId());
                doesProduitExist(cd.getIdProduit());
                cd.setIdCommande(newCommande.getId());

                //verifications pour les commandes prod associées à cette commande detail.
                //verification de l'existence de chaque producteurs affectés à la commande et si leurs stock >= quantite commandeProd
                ArrayList<CommandeProducteur> commandesProd = (ArrayList<CommandeProducteur>) mapEntry.getValue();
                for (CommandeProducteur cp : commandesProd) {
                    //System.out.println(cp.toString());
                    doesCommandeProdExist(cp.getId());
                    doesProducteurExist(cp.getIdProducteur());
                    Optional<Producteur> producteur = producteurRepository.findById(cp.getIdProducteur());
                    Optional<Client> client = clientRepository.findById(commandeInfo.getCommande().getIdClient());
                    if(doesProducteurCanDelivery(producteur.get(), client.get())){
                        if (doesPorducteurHaveEnough(cp.getIdProducteur(), cd.getIdProduit(), cp.getQuantite())) {
                            producteurRepository.updateQteProduit(cp.getIdProducteur(), cd.getIdProduit(), cp.getQuantite());
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * verifie que la commande n'existe pas dans la base de données
     *
     * @param id
     * @return true si la commande n'existe pas
     * @throws Exception si la commande existe deja
     */
    public boolean commandeExist(Long id) throws Exception {
        if (commandeRepository.findById(id).isEmpty()) {
            return true;
        } else throw new Exception("La commande n°" + id + "existe  deja");
    }


    /**
     * Récupere toutes les commandesProd et commandesDetail de la commandeInfo pour les mettre trier via une
     * HasMap<CommandeDetail, ArrayList <CommandeProducteur>>, cela permet d'associer a chaque commandeDetail les commandeProducteur
     * correspondentes.
     *
     * @param commandeInfo
     * @return HashMap<CommandeDetail, ArrayList <CommandeProducteur>>
     */
    public HashMap<CommandeDetail, ArrayList<CommandeProducteur>> getCommandesProducteurByCommandeDetail(CommandeInfo commandeInfo) {
        HashMap resultHashMap = new HashMap();
        Collection<CommandeDetail> commandeDetails = commandeInfo.getCommandesDetails();
        for (CommandeDetail cd : commandeDetails) {
            Long id = cd.getId();
            ArrayList<CommandeProducteur> commandeProducteurs = new ArrayList<>();
            //pour chaque commandeDetail on crée une arrayList<CommandeProducteur>.
            for (CommandeProducteur cp : commandeInfo.getCommandesProducteur()) {
                //si la commandeProducteur à un idCommandeDetail = l'id de la CommandeDetail on la met dans l'arrayList
                if (Objects.equals(cp.getIdCommandeDetails(), id)) {
                    commandeProducteurs.add(cp);
                }

                resultHashMap.put(cd, commandeProducteurs);
            }
        }
        return resultHashMap;
    }

    /**
     * Verifie que le producteur existe bien.
     *
     * @param idProducteur le producteur
     * @return true si le producteur existe.
     */
    public boolean doesProducteurExist(Long idProducteur) throws Exception {
        if (producteurRepository.findById(idProducteur).isPresent()) {
            return true;
        } else throw new Exception("Le producteur n°" + idProducteur + " n'existe pas");
    }


    /**
     * recupere la quantité possédée par un producteur d'un produit
     *
     * @param idProduit
     * @param idProducteur
     * @return float quantite
     */


    /**
     * Verifie que le client existe dans la base de donnees
     *
     * @param idClient
     * @return true si le client exist
     * @throws Exception si le client n'existe pas.
     */
    public boolean doesClientExist(Long idClient) throws Exception {
        if (clientRepository.findById(idClient).isPresent()) {
            return true;
        } else throw new Exception("Le client n°" + idClient + " n'existe pas");
    }

    /**
     * Verifie que le produit existe dans la base de donnees
     *
     * @param idProduit
     * @return true si le produit existe
     * @throws Exception si le produit n'existe pas
     */
    public boolean doesProduitExist(Long idProduit) throws Exception {
        if (produitRepository.findById(idProduit).isPresent()) {
            return true;
        } else throw new Exception("Le produit n°" + idProduit + " n'existe pas");
    }

    @Autowired
    private GeoPortailApiService geoPortailApiService;

    /**
     * verifie la distantce entre un producteur et son client
     * @param producteur
     * @param client
     * @return true si rayon_livraison > distance
     * @throws Exception si rayon_livraison inferieur a distance
     */
    public boolean doesProducteurCanDelivery(Producteur producteur, Client client) throws Exception {
        double meter =geoPortailApiService.verifDistanceBetweenProducteurAndClient(producteur.getLatitude(), producteur.getLongitude(), client.getLatitude(), client.getLongitude());
        if(meter > producteur.getRayon_Livraison()){
            throw new Exception("Le client se situe trop loin du producteur : "+ producteur.getLibelle()+","+ producteur.getId_Producteur()+", rayon de livraison "+ producteur.getRayon_Livraison()+" km."+ "\ndistance avec le client :"+client.getAdresse()+"\n" + meter + " km.");
        } else return true;
    }

    /**
     * Verifie que le producteur possede le produit avec une quantite suffisante.
     *
     * @param idproducteur
     * @param idProduit
     * @param quantite
     * @return true si il possede le produit en quantite suffisante
     * @throws Exception si le producteur ne possede pas le produit en quantite suffisante
     * @throws Exception si le producteur ne possede pas le produit
     */
    public boolean doesPorducteurHaveEnough(Long idproducteur, Long idProduit, Float quantite) throws Exception {
        //System.out.println(produitRepository.findProduitsByProducteur(idproducteur).toString());
        if (produitRepository.findProduitsByProducteur(idproducteur).contains(produitRepository.findById(idProduit).get())) {
            Optional<Float> optionalQte = producteurRepository.getQteProduit(idproducteur, idProduit);
            if (optionalQte.isPresent() && optionalQte.get() >= quantite) {
                return true;
            }
            throw new Exception("le producteur n°" + idproducteur + " n'a pas le produit n°" + idProduit + " en quantite suffiante");
        }
        throw new Exception("Le producteur n°" + idproducteur + " : " + producteurRepository.findById(idproducteur).get().getLibelle() + " ne possede pas le produit n°" + idProduit + " : " + produitRepository.findById(idProduit).get().getLibelle());
    }

    @Autowired
    private CommandeProducteurRepository commandeProducteurRepository;

    @Autowired
    private CommandeDetailRepository commandeDetailRepository;

    /**
     * Verifie que la commande n'existe pas dans la base de donnees
     *
     * @param idCommandeProd
     * @return true si la commande n'existe pas
     * @throws Exception si la commande existe deja
     */
    public boolean doesCommandeProdExist(Long idCommandeProd) throws Exception {
        if (!commandeProducteurRepository.existsById(idCommandeProd)) {
            return true;
        } else throw new Exception("La commandeProducteur n°" + idCommandeProd + " existe deja");
    }

    /**
     * Verifie que la commande n'existe pas dans la base de données
     *
     * @param idCommandeDetail
     * @return true si elle n'existe pas
     * @throws Exception si la commande existe deja
     */
    public boolean doesCommandeDetailExist(Long idCommandeDetail) throws Exception {
        if (!commandeDetailRepository.existsById(idCommandeDetail)) {
            return true;
        } else throw new Exception("La commandeDetail n°" + idCommandeDetail + " existe deja");
    }



    /**
     * Verification et modification entre la commandeInfo de base et l'update
     * puis appel de la methode verifCommandeInfo() pour reverifier les données dans la nouvelle commandeInfo
     * @param updateCommandeInfo l'update de la commandeInfo
     * @return la CommandeInfo mis a jour l'originale sinon
     * @throws Exception
     */
    public CommandeInfo verifCommandeInfoUpdate(CommandeInfo updateCommandeInfo) throws Exception {
        //on recupere la commandeInfo originale avec l'id de la commande l'updateCommandeInfo car l'id d'une commande ne change pas.
        CommandeInfo originalCommandeInfo = getCommandeInfo(updateCommandeInfo.getCommande().getId());

        if(isValidNewCommandeInfo(updateCommandeInfo)){
            //suppression des commandes details en trop
            for (CommandeDetail cd : originalCommandeInfo.getCommandesDetails()) {
                if(updateCommandeInfo.getCommandesDetails().contains(cd)){
                } else {
                    commandeDetailRepository.delete(cd);
                }
            }

            for (CommandeProducteur cp: originalCommandeInfo.getCommandesProducteur()) {
                if(updateCommandeInfo.getCommandesProducteur().contains(cp)){
                }else {
                    Optional<CommandeDetail> temp =  commandeDetailRepository.findById(cp.getIdCommandeDetails());
                    System.out.println("========================"+temp.get().getIdProduit() + " " + cp.getIdProducteur() +" "+ cp.getQuantite());
                    commandeProducteurRepository.reatributStockToProducteur(cp.getIdProducteur(), temp.get().getIdProduit() ,cp.getQuantite());
                    commandeProducteurRepository.delete(cp);
                }
            }

        }
        return originalCommandeInfo;
    }

    public boolean isValidNewCommandeInfo(CommandeInfo commandeInfo) throws Exception {

        //verification de l'existence du client
        Commande newCommande = commandeInfo.getCommande();
        if (doesClientExist(newCommande.getIdClient())) {
            //verification de l'existence des produits

            HashMap<CommandeDetail, ArrayList<CommandeProducteur>> hashVerif = getCommandesProducteurByCommandeDetail(commandeInfo);
            for (Map.Entry mapEntry : hashVerif.entrySet()) {
                CommandeDetail cd = (CommandeDetail) mapEntry.getKey();
                doesProduitExist(cd.getIdProduit());
                cd.setIdCommande(newCommande.getId());

                //verifications pour les commandes prod associées à cette commande detail.
                //verification de l'existence de chaque producteurs affectés à la commande et si leurs stock >= quantite commandeProd
                ArrayList<CommandeProducteur> commandesProd = (ArrayList<CommandeProducteur>) mapEntry.getValue();
                for (CommandeProducteur cp : commandesProd) {
                    doesProducteurExist(cp.getIdProducteur());
                    Optional<Producteur> producteur = producteurRepository.findById(cp.getIdProducteur());
                    Optional<Client> client = clientRepository.findById(commandeInfo.getCommande().getIdClient());
                    if(doesProducteurCanDelivery(producteur.get(), client.get())){
                        if (doesPorducteurHaveEnough(cp.getIdProducteur(), cd.getIdProduit(), cp.getQuantite())) {
                            producteurRepository.updateQteProduit(cp.getIdProducteur(), cd.getIdProduit(), cp.getQuantite());
                        }
                    }
                }
            }
        }
        return true;
    }



    /**
     * Supprime toute la commadeInfo
     *
     * @param idCommande id de la commande info a supprimer
     * @throws Exception si non
     */
    public void deletCommandeInfo(Long idCommande) throws Exception {

        CommandeInfo commandeInfoToDelete = getCommandeInfo(idCommande);

        //on reatribut le stock au producteur puis on supprime la commande producteur
        for (CommandeProducteur cp : commandeInfoToDelete.getCommandesProducteur()) {
            //si une commande producteur est dans la base de donnée alors elle est forcement liee a une commande detail.
            //on a besoin de la commande detail en question pour recuperer le produit
            Optional<CommandeDetail> cd = commandeDetailRepository.findById(cp.getIdCommandeDetails());
            System.out.println("========================"+cd.get().getIdProduit());
            commandeProducteurRepository.reatributStockToProducteur(cp.getIdProducteur(),cd.get().getIdProduit() ,cp.getQuantite());

        }

        //on supprime les commandes details
        for (CommandeDetail cd : commandeInfoToDelete.getCommandesDetails()) {
            commandeDetailService.deletCommandeDetail(cd);
        }
        //on supprime la commande
        commandeRepository.delete(commandeInfoToDelete.getCommande());
    }

    /**
     * supprime la commandeInfo sans supprimer la commande
     * Utile dans le cas de l'update de la commandeInfo car
     * dans l'update tout est supprimé pour etre remplacé mais si la commande n'existe plus le save va auto incrementer l'objet au lieu de juste faire une update.
     * @param commandeInfo la commandeInfo à supprimer
     * @return la commande de la commandeInfo
     * @throws Exception
     */
    public Commande deleteCommandeInfoFOrUpdate(CommandeInfo commandeInfo) throws Exception {
       CommandeInfo toDelet = getCommandeInfo(commandeInfo.getCommande().getId());
       Commande toSave = toDelet.getCommande();

        //on reatribut le stock au producteur puis on supprime la commande producteur
        for (CommandeProducteur cp : toDelet.getCommandesProducteur()) {
            //si une commande producteur est dans la base de donnée alors elle est forcement liee a une commande detail.
            //on a besoin de la commande detail en question pour recuperer le produit
            Optional<CommandeDetail> cd = commandeDetailRepository.findById(cp.getIdCommandeDetails());
            System.out.println("========================"+cd.get().getIdProduit());
            commandeProducteurRepository.reatributStockToProducteur(cp.getIdProducteur(),cd.get().getIdProduit() ,cp.getQuantite());
            commandeProducteurRepository.delete(cp);
        }

        //on supprime les commandes details et ses commandes prod.
        for (CommandeDetail cd : toDelet.getCommandesDetails()) {
            commandeDetailService.deletCommandeDetail(cd);
        }

        return toSave;

    }

    /**
     * retrouve la commande info dans sa totalité à partir de l'id d'une commande
     *
     * @param idCommande de la commande
     * @return la commande info
     */
    public CommandeInfo getCommandeInfo(Long idCommande) throws Exception {
        //la commande info a reconstituer
        CommandeInfo savedCommandeInfo = new CommandeInfo();
        if (commandeRepository.findById(idCommande).isPresent()) {
            Optional<Commande> temp = commandeRepository.findById(idCommande);
            savedCommandeInfo.setCommande(temp.get());

            //on recupere toutes les commandes details associées à cette commande
            savedCommandeInfo.setCommandesDetails((Collection<CommandeDetail>) commandeDetailRepository.findAllByIdCommande(idCommande));

            ArrayList<CommandeProducteur> commandeProducteurs = new ArrayList<>();
            //on parcour toutes les commandesDetails pour retrouver toutes les commandesProducteur associees.
            for (CommandeDetail cd : savedCommandeInfo.getCommandesDetails()) {
                Collection<CommandeProducteur> commandesProd = (Collection<CommandeProducteur>) commandeProducteurRepository.findCommandeProdByCommandeDetail(cd.getId());
                commandeProducteurs.addAll(commandesProd);
            }
            savedCommandeInfo.setCommandesProducteur(commandeProducteurs);
            return savedCommandeInfo;
        }
         else throw new Exception("La commande n°" + idCommande + " n'existe pas.");
    }

    /**
     * Récupère toutes les commandes liées à un produit d'id idProduit
     * @param idProduit
     * @return Iterable<Commande> les commandes liées au produit
     * @throws Exception si le produit n'existe pas
     */
    public Iterable<Commande> getAllCommandesByProduit(Long idProduit) throws Exception {
        if (produitRepository.existsById(idProduit)) {
            return commandeRepository.findAllCommandesByProduit(idProduit);
        } else throw new Exception("Le produit n°" + idProduit + " n'existe pas.");
    }

}
