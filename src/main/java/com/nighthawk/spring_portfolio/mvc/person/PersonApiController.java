package com.nighthawk.spring_portfolio.mvc.person;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;

/**
 * This class provides RESTful API endpoints for managing Person entities.
 * It includes endpoints for creating, retrieving, updating, and deleting Person
 * entities.
 */
@RestController
@RequestMapping("/api")
public class PersonApiController {
    /*
     * #### RESTful API REFERENCE ####
     * Resource: https://spring.io/guides/gs/rest-service/
     */

    /**
     * Repository for accessing Person entities in the database.
     */
    @Autowired
    private PersonJpaRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Service for managing Person entities.
     */
    @Autowired
    private PersonDetailsService personDetailsService;

    /**
     * Retrieves a Person entity by current user of JWT token.
     * 
     * @return A ResponseEntity containing the Person entity if found, or a
     *         NOT_FOUND status if not found.
     */
    @GetMapping("/person/get")
    public ResponseEntity<Person> getPerson(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername(); // Email is mapped/unmapped to username for Spring Security

        // Find a person by username
        Person person = repository.findByEmail(email);

        // Return the person if found
        if (person != null) {
            return new ResponseEntity<>(person, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Retrieves all the Person entities in the database, people
     * 
     * @return A ResponseEntity containing a list for Person entities
     */
    @GetMapping("/people")
    public ResponseEntity<List<Person>> getPeople() {
        return new ResponseEntity<>( repository.findAllByOrderByNameAsc(), HttpStatus.OK);
    }

    /**
     * Retrieves a Person entity by its ID.
     *
     * @param id The ID of the Person entity to retrieve.
     * @return A ResponseEntity containing the Person entity if found, or a
     *         NOT_FOUND status if not found.
     */
    @GetMapping("/person/{id}")
    public ResponseEntity<Person> getPerson(@PathVariable long id) {
        Optional<Person> optional = repository.findById(id);
        if (optional.isPresent()) { // Good ID
            Person person = optional.get(); // value from findByID
            return new ResponseEntity<>(person, HttpStatus.OK); // OK HTTP response: status code, headers, and body
        }
        // Bad ID
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Delete a Person entity by its ID.
     *
     * @param id The ID of the Person entity to delete.
     * @return A ResponseEntity containing the Person entity if deleted, or a
     *         NOT_FOUND status if not found.
     */
    @DeleteMapping("/person/{id}")
    public ResponseEntity<Person> deletePerson(@PathVariable long id) {
        Optional<Person> optional = repository.findById(id);
        if (optional.isPresent()) { // Good ID
            Person person = optional.get(); // value from findByID
            repository.deleteById(id); // value from findByID
            return new ResponseEntity<>(person, HttpStatus.OK); // OK HTTP response: status code, headers, and body
        }
        // Bad ID
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /*
     * DTO (Data Transfer Object) to support POST request for postPerson method
     * .. represents the data in the request body
     */
    @Getter
    public static class PersonDto {
        private String email;
        private String password;
        private String name;
        private String dob;
        private String pfp;
        private Boolean kasmServerNeeded; 
    }

    /**
     * Create a new Person entity.
     * 
     * @param personDto
     * @return A ResponseEntity containing a success message if the Person entity is
     *         created, or a BAD_REQUEST status if not created.
     */
    @PostMapping("/person/create")
    public ResponseEntity<Object> postPerson(@RequestBody PersonDto personDto) {
        // Validate dob input
        Date dob;
        try {
            dob = new SimpleDateFormat("MM-dd-yyyy").parse(personDto.getDob());
        } catch (Exception e) {
            return new ResponseEntity<>(personDto.getDob() + " error; try MM-dd-yyyy", HttpStatus.BAD_REQUEST);
        }
        // A person object WITHOUT ID will create a new record in the database
        Person person = new Person(personDto.getEmail(), personDto.getPassword(), personDto.getName(), dob, "USER", true, personDetailsService.findRole("USER"));

        personDetailsService.save(person);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject responseObject = new JSONObject();
        responseObject.put("response",personDto.getEmail() + " is created successfully");

        String reponseString = responseObject.toString();

        return new ResponseEntity<>(reponseString,responseHeaders, HttpStatus.OK);
    }





@PostMapping(value = "/person/update", produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<Object> updatePerson(Authentication authentication, @RequestBody final PersonDto personDto) {
    // Get the email of the current user from the authentication context
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String email = userDetails.getUsername(); // Assuming email is used as the username in Spring Security

    // Find the person by email
    Optional<Person> optionalPerson = Optional.ofNullable(repository.findByEmail(email));
    if (optionalPerson.isPresent()) {
        Person existingPerson = optionalPerson.get();

        // Update fields only if they're provided in personDto
        if (personDto.getEmail() != null) {
            existingPerson.setEmail(personDto.getEmail());
        }
        if (personDto.getPassword() != null) {
            existingPerson.setPassword(passwordEncoder.encode(personDto.getPassword()));

        }
    
        if (personDto.getName() != null) {
            existingPerson.setName(personDto.getName());
        }
        if (personDto.getPfp() != null) {
            existingPerson.setPfp(personDto.getPfp());
        }
        if (personDto.getKasmServerNeeded() != null) {
            existingPerson.setKasmServerNeeded(personDto.getKasmServerNeeded());
        }
        // Save the updated person back to the repository
        Person updatedPerson = repository.save(existingPerson);

        // Return the updated person entity
        return new ResponseEntity<>(updatedPerson, HttpStatus.OK);
    }

    // Return NOT_FOUND if person not found
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
}


    /**
     * Search for a Person entity by name or email.
     * 
     * @param map of a key-value (k,v), the key is "term" and the value is the
     *            search term.
     * @return A ResponseEntity containing a list of Person entities that match the
     *         search term.
     */

    @PostMapping(value = "/people/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> personSearch(@RequestBody final Map<String, String> map) {
        // extract term from RequestEntity
        String term = (String) map.get("term");

        // JPA query to filter on term
        List<Person> list = repository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term);

        // return resulting list and status, error checking should be added
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // @PostMapping(value = "/person/setSections", produces = MediaType.APPLICATION_JSON_VALUE)
    // public ResponseEntity<?> setSections(@AuthenticationPrincipal UserDetails userDetails, @RequestBody final List<SectionDTO> sections) {
    //     // Check if the authentication object is null
    //     if (userDetails == null) {
    //         return ResponseEntity
    //                 .status(HttpStatus.UNAUTHORIZED)
    //                 .body("Error: Authentication object is null. User is not authenticated.");
    //     }
        
    //     String email = userDetails.getUsername();
        
    //     // Manually wrap the result in Optional.ofNullable
    //     Optional<Person> optional = Optional.ofNullable(repository.findByEmail(email));
    //     if (optional.isPresent()) {
    //         Person person = optional.get();

    //         // Get existing sections and ensure it is not null
    //         Collection<PersonSections> existingSections = person.getSections();
    //         if (existingSections == null) {
    //             existingSections = new ArrayList<>();
    //         }

    //         // Add  sections
    //         for (SectionDTO sectionDTO : sections) {
    //             if (!existingSections.stream().anyMatch(s -> s.getName().equals(sectionDTO.getName()))) {
    //                 PersonSections newSection = new PersonSections(sectionDTO.getName(), sectionDTO.getAbbreviation(), sectionDTO.getYear());
    //                 existingSections.add(newSection);
    //             } else {
    //                 return ResponseEntity
    //                         .status(HttpStatus.CONFLICT)
    //                         .body("Error: Section with name '" + sectionDTO.getName() + "' already exists.");
    //             }
    //         }

    //         // Persist updated sections
    //         person.setSections(existingSections);
    //         repository.save(person);

    //         // Return updated Person
    //         return ResponseEntity.ok(person);
    //     }

    //     // Person not found
    //     return ResponseEntity
    //             .status(HttpStatus.NOT_FOUND)
    //             .body("Error: Person not found with email: " + email);
    // }


    @PutMapping("/person/{id}")
    public ResponseEntity<Object> updatePerson(@PathVariable long id, @RequestBody PersonDto personDto) {
        Optional<Person> optional = repository.findById(id);
        if (optional.isPresent()) {  // If the person with the given ID exists
            Person existingPerson = optional.get();

            // Update the existing person's details
            existingPerson.setEmail(personDto.getEmail());
            existingPerson.setPassword(personDto.getPassword());
            existingPerson.setName(personDto.getName());
            
            // Optional: Update other fields if they exist in Person
            existingPerson.setPfp(personDto.getPfp());
            existingPerson.setKasmServerNeeded(personDto.getKasmServerNeeded());

            // Save the updated person back to the repository
            repository.save(existingPerson);

            // Return the updated person entity
            return new ResponseEntity<>(existingPerson, HttpStatus.OK);
        }

        // Return NOT_FOUND if the person with the given ID does not exist
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Adds stats to the Person table
     * 
     * @param stat_map is a JSON object, example format:
        {"health":
            {"date": "2021-01-01",
            "measurements":
                {   
                    "weight": "150",
                    "height": "70",
                    "bmi": "21.52"
                }
            }
        }
     * @return A ResponseEntity containing the Person entity with updated stats, or
     *         a NOT_FOUND status if not found.
     */
    @PostMapping(value = "/person/setStats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Person> personStats(Authentication authentication, @RequestBody final Map<String,Object> stat_map) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername(); // Email is mapped/unmapped to username for Spring Security

        // Find a person by username
        Optional<Person> optional = Optional.ofNullable(repository.findByEmail(email));
        if (optional.isPresent()) { // Good ID
            Person person = optional.get(); // value from findByID

            // Get existing stats
            Map<String, Map<String, Object>> existingStats = person.getStats();

            // Iterate through each key in the incoming stats
            for (String key : stat_map.keySet()) {
                // Extract the stats for this key from the incoming stats
                Map<String, Object> incomingStats = (Map<String, Object>) stat_map.get(key);

                // Extract the date and attributes from the incoming stats
                String date = (String) incomingStats.get("date");
                Map<String, Object> attributeMap = new HashMap<>(incomingStats);
                attributeMap.remove("date");

                // New key test.
                if (!existingStats.containsKey(key)) {
                    // Add the new key
                    existingStats.put(key, new HashMap<>());
                }

                // Existing date test.
                if (existingStats.get(key).containsKey(date)) { // Existing date, update the attributes
                    // Make a map inside of existingStats to hold the current attributes for the
                    // date
                    Map<String, Object> existingAttributes = (Map<String, Object>) existingStats.get(key).get(date);
                    // Combine the existing attributes with these new attributes
                    existingAttributes.putAll(attributeMap);
                } else { // New date, add the new date and attributes
                    existingStats.get(key).put(date, attributeMap);
                }
            }

            // Set and save the updated stats
            person.setStats(existingStats);
            repository.save(person); // conclude by writing the stats updates to the database

            // return Person with update to Stats
            return new ResponseEntity<>(person, HttpStatus.OK);
        }
        // return Bad ID
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}

