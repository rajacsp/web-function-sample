package org.packtpub;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.springframework.http.codec.multipart.Part;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserHandler {

	private final UserRepository userRepository;
	
	public UserHandler(UserRepository userRepository){
		this.userRepository = userRepository;
	}
	
	public Mono<ServerResponse> getAllUsers(ServerRequest request){
		Flux<User> users = this.userRepository.getAllUsers();
		return ServerResponse.ok().contentType(APPLICATION_JSON).body(users, User.class);		
	}
	
	public Mono<ServerResponse> getUser(ServerRequest request){
		
		int userId = Integer.valueOf(request.pathVariable("id"));
		
		Mono<ServerResponse> notFound = ServerResponse.notFound().build();
		Mono<User> userMono = this.userRepository.getUser(userId);
		
		return userMono
				.flatMap(user -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(user)))
				.switchIfEmpty(notFound);		
	}
	
	public Mono<ServerResponse> createUser(ServerRequest request) {
		Mono<User> user = request.bodyToMono(User.class);
		return ServerResponse.ok().build(this.userRepository.saveUser(user));
	}
	
	public Mono<ServerResponse> updateUser(ServerRequest request) {
		Mono<User> user = request.bodyToMono(User.class);
		return ServerResponse.ok().build(this.userRepository.saveUser(user));
	}
	
	public Mono<ServerResponse> upload(ServerRequest request) {
		
		Mono<Part> part = request.body(BodyExtractors.toMono(Part.class));
		
		//Mono<FilePart> part = request.bodyToMono(FilePart.class);
		
		//part.log().block();
		System.out.println("trap : "+part);//fileName);
		
		//FilePart filePart = part.log().block();
		
		//String contentType = filePart.headers().getContentType().toString();        
        //String fileName = filePart.filename();
        
        System.out.println("inside upload : ");//fileName);        
        //filePart.transferTo(new File("c:\\test\\"+fileName));
		
		return ServerResponse.ok().build();
	}
	
	public Mono<ServerResponse> generateToken(ServerRequest request){
		
		String token = createJWTToken("one", "two", ttlMillis);
		
		Mono<String> userMono = Mono.justOrEmpty(token);
		Mono<ServerResponse> notFound = ServerResponse.notFound().build();
		return userMono
				.flatMap(user -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(user)))
				.switchIfEmpty(notFound);
	}
	
	public Mono<ServerResponse> verifyToken(ServerRequest request){
		
		String token = request.headers().header("token").get(0);		
		System.out.println("token : "+token);
		
		String issuer = verifyIssuer(token);		
		System.out.println("issuer : "+issuer);
		
		Mono<String> userMono = Mono.justOrEmpty(issuer);
		Mono<ServerResponse> notFound = ServerResponse.notFound().build();
		return userMono
				.flatMap(user -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(user)))
				.switchIfEmpty(notFound);
	}
	
	public Mono<ServerResponse> deleteUser(ServerRequest request) {		
		int userId = Integer.valueOf(request.pathVariable("id"));
		return ServerResponse.ok().build(this.userRepository.deleteUser(userId));
	}
	
	private static final String secretKey= "some_secret_key";
	private static final long ttlMillis = 1000 * 60 * 10; //1 min
	public String createJWTToken(String subject, String issuer, long ttlMillis) {
		 
		 SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		 
	    //The JWT signature algorithm we will be using to sign the token
		long nowMillis = System.currentTimeMillis();
		
		//System.out.println("{current time  " + nowMillis);
	    
		Date now = new Date(nowMillis);
		// System.out.println("{current date  " + now);	    
	    
	    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);
	    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
	    
	    JwtBuilder builder = Jwts.builder()
               .setSubject(subject)
               .setIssuer(issuer)
               .signWith(signatureAlgorithm, signingKey);

	    if (ttlMillis >= 0) {
		    long expMillis = nowMillis + ttlMillis;
		        Date exp = new Date(expMillis);
		        builder.setExpiration(exp);
		    }
   
	    
	    return builder.compact();
	}
	
	public String verifyIssuer(String token){
		Claims claims = Jwts.parser()         
			       .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
			       .parseClaimsJws(token).getBody();
		
		return claims.getIssuer();				
	}
}
