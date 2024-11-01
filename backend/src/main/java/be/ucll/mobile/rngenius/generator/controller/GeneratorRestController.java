package be.ucll.mobile.rngenius.generator.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import be.ucll.mobile.rngenius.auth.jwt.JwtUtil;
import be.ucll.mobile.rngenius.generator.model.Generator;
import be.ucll.mobile.rngenius.generator.model.GeneratorException;
import be.ucll.mobile.rngenius.generator.service.GeneratorService;
import be.ucll.mobile.rngenius.generator.service.GeneratorServiceAuthorizationException;
import be.ucll.mobile.rngenius.generator.service.GeneratorServiceException;
import be.ucll.mobile.rngenius.option.model.Option;
import be.ucll.mobile.rngenius.user.model.UserException;
import be.ucll.mobile.rngenius.user.service.UserServiceException;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = {"*"})
@RequestMapping("/generator")
public class GeneratorRestController {

    @Autowired
    private GeneratorService generatorService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/{id}")
    public Generator getGenerator(@PathVariable Long id, @RequestHeader("Authorization") String token) throws GeneratorServiceException, GeneratorServiceAuthorizationException {
        Long requesterId = jwtUtil.retrieveRequesterId(token);
        return generatorService.getGeneratorById(id, requesterId);
    }

    @GetMapping("/myGenerators")
    public List<Generator> getMyGenerators(@RequestHeader("Authorization") String token) throws GeneratorServiceException {
        Long requesterId = jwtUtil.retrieveRequesterId(token);
        return generatorService.getMyGenerators(requesterId);
    }

    @PostMapping("/add")
    public void addGenerator(@RequestBody @Valid Generator generator, @RequestHeader("Authorization") String token) throws GeneratorServiceException, UserServiceException {
        Long requesterId = jwtUtil.retrieveRequesterId(token);
        generatorService.addGenerator(generator, requesterId);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteGenerator(@PathVariable Long id, @RequestHeader("Authorization") String token) throws GeneratorServiceException, GeneratorServiceAuthorizationException {
        Long requesterId = jwtUtil.retrieveRequesterId(token);
        generatorService.deleteGeneratorById(id, requesterId);
    }

    @PutMapping("/addOption/{generatorId}")
    public void addOption(@PathVariable Long generatorId, @RequestBody @Valid Option option, @RequestHeader("Authorization") String token) throws GeneratorServiceException, GeneratorServiceAuthorizationException {
        Long requesterId = jwtUtil.retrieveRequesterId(token);
        generatorService.addGeneratorOption(generatorId, option, requesterId);
    }

    @PutMapping("/deleteOption/{optionId}")
    public void deleteOption(@PathVariable Long optionId, @RequestParam String category, @RequestHeader("Authorization") String token) throws GeneratorServiceException, GeneratorServiceAuthorizationException {
        Long requesterId = jwtUtil.retrieveRequesterId(token);
        generatorService.deleteCategorizedGeneratorOption(optionId, category, requesterId);
    }   

    @GetMapping("/generate/{id}")
    public Option generate(@PathVariable Long id, @RequestHeader("Authorization") String token) throws GeneratorServiceException, GeneratorException, GeneratorServiceAuthorizationException {
        Long requesterId = jwtUtil.retrieveRequesterId(token);
        return generatorService.generateOption(id, requesterId);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ MethodArgumentNotValidException.class})
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getFieldErrors().forEach((error) -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put("field", fieldName);
            errors.put("message", errorMessage);
        });
        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ UserServiceException.class})
    public Map<String, String> handleServiceExceptions(UserServiceException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("field", ex.getField());
        errors.put("message", ex.getMessage());
        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ GeneratorServiceException.class})
    public Map<String, String> handleServiceExceptions(GeneratorServiceException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("field", ex.getField());
        errors.put("message", ex.getMessage());
        return errors;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({ GeneratorServiceAuthorizationException.class})
    public Map<String, String> handleServiceExceptions(GeneratorServiceAuthorizationException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("field", ex.getField());
        errors.put("message", ex.getMessage());
        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ GeneratorException.class})
    public Map<String, String> handleServiceExceptions(UserException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("field", ex.getField());
        errors.put("message", ex.getMessage());
        return errors;
    }  
}