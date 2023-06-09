package com.lifat.CircuitsCourtsApi.controller;

import com.lifat.CircuitsCourtsApi.model.Tournee;
import com.lifat.CircuitsCourtsApi.service.TourneeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
/**
 * End point de l'api pour les tournées.
 * Le nom des méthodes décrit leurs fonctionnement
 */
public class TourneeController {
    @Autowired
    private TourneeService tourneeService;

    @GetMapping("/tournees")
    public ResponseEntity<?> getAllTournees(){
        ArrayList<Tournee> tournees = (ArrayList<Tournee>) tourneeService.getTournee();
        if (tournees.isEmpty()){
            return ResponseEntity.badRequest().body("aucune tournees");
        }
        else return ResponseEntity.ok().body(tournees);
    }


    @GetMapping("/tournee/{id}")
    public ResponseEntity<?> getTournee(@PathVariable Long id){
        if(tourneeService.getTourneeById(id).isPresent()){
            return ResponseEntity.ok().body(tourneeService.getTourneeById(id));
        }
        else return ResponseEntity.badRequest().body("La tournée n°" + id + "n'existe pas.");
    }

    @DeleteMapping("/tournee/{id}")
    public ResponseEntity<?> deletTournee(@PathVariable Long id){
        Tournee tournee = tourneeService.getTourneeById(id).get();
        if(tourneeService.getTourneeById(id).isPresent()){
            tourneeService.deletTournee(tournee);
            return ResponseEntity.noContent().build();
        } else return ResponseEntity.badRequest().body("La tournee n°" + id + "n'existe pas");
    }


    //TODO
    @PostMapping("/tournee/verif")
    public ResponseEntity<?> verifTournee(@RequestBody Tournee tournee){
        try{
            tourneeService.isTourneeValid(tournee);
            tourneeService.save(tournee);
            return ResponseEntity.ok().body(tournee);
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
