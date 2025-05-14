package com.hcc.services;

//import java.util.Optional;
//
//
//// uncomment this class once you have created all of the needed parts
//@Service
//public class UserDetailServiceImpl implements UserDetailsService {
//    @Autowired
//    CustomPasswordEncoder passwordEncoder;
//
//    @Autowired
//     UserRepository userRepo;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
//        Optional<User> userOpt = userRepo.findByUsername(username);
//        user.setUsername(username);
//        user.setPassword(passwordEncoder.getPasswordEncoder().encode("asdfasdf"));
//        return userOpt.orElseThrow(() -> new UserNotFoundException("Invalid Credentials"));
//    }
//}
