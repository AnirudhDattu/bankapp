package com.bank.izbank.UserInfo;

//UserContext class manages the state of a user (presumably in some banking system) using the State design pattern,
// where different behaviors are encapsulated in separate state objects (UserTypeState implementations),
// and the context (UserContext) delegates behavior to the current state.
public class UserContext implements UserTypeState {
    private UserTypeState userState;

    public void setState(UserTypeState state) {
        this.userState=state;
    }

    public UserTypeState getState() {
        return this.userState;
    }
    @Override
    public void TypeChange(User user) {
        this.userState.TypeChange(user);
    }



}
