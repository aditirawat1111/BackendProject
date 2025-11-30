# Authentication vs Authorization - Explanation

## Authentication (Who you are?)
**Question Answered**: "Are you who you claim to be?"

**Implementation in Step 3:**
- User provides email/password
- System verifies credentials
- System generates JWT token
- Token proves user identity in future requests

**Tools Used:**
- Spring Security (authentication framework)
- JWT/JJWT (token-based authentication)
- BCrypt (password hashing)

## Authorization (What you can do?)
**Question Answered**: "What are you allowed to do?"

**Foundation Set in Step 3:**
- User entity has `role` field
- Roles can be "USER" or "ADMIN"
- Ready for authorization checks

**Tools Used:**
- Spring Security (role-based access control)
- `@PreAuthorize` annotations (method-level)
- SecurityConfig (URL-based rules)

**Future Implementation:**
- Add role-based access control
- Protect admin endpoints
- Ensure users only access their own data

---

## Tools Breakdown

### 1. Spring Security
- **Purpose**: Complete authentication & authorization framework
- **Dependency**: `spring-boot-starter-security`
- **Used For**: Both authentication AND authorization

### 2. JWT (JSON Web Tokens)
- **Library**: `io.jsonwebtoken:jjwt` (v0.12.3)
- **Purpose**: Stateless authentication tokens
- **Used For**: Authentication only

### 3. BCrypt
- **Source**: Included with Spring Security
- **Purpose**: Password hashing
- **Used For**: Authentication (password security)

---

## Step 3 Implementation Focus

✅ **Authentication** - Fully implemented
- JWT token generation
- Token validation
- User identity verification
- Password hashing

🔨 **Authorization Foundation** - Prepared
- Role field exists
- Ready for role-based checks
- Can be implemented after Step 3

