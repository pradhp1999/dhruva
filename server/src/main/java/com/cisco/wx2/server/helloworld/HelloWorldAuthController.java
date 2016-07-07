package com.cisco.wx2.server.helloworld;

import com.cisco.wx2.server.auth.AuthInfo;
import com.cisco.wx2.server.auth.AuthorizationRole;
import com.cisco.wx2.server.auth.ng.*;
import com.cisco.wx2.server.auth.ng.annotation.*;
import com.cisco.wx2.util.OrgId;
import org.springframework.web.bind.annotation.*;
import org.versly.rest.wsdoc.AuthorizationScope;
import org.versly.rest.wsdoc.DocumentationRestApi;
import org.versly.rest.wsdoc.DocumentationScope;
import org.versly.rest.wsdoc.DocumentationTraits;

import javax.servlet.http.HttpServletRequest;

import static com.cisco.wx2.server.auth.ng.AuthPredicates.*;

/*
 * For more information:
 *
 * https://sqbu-github.cisco.com/WebExSquared/cloud-apps/wiki/declarativeauth
 */
@DocumentationRestApi(
        id = "hello-world-auth",
        title = "Hello World Authorization",
        version = "v1",
        mount = "")
@DocumentationScope(DocumentationScope.PRIVATE)
@DocumentationTraits(DocumentationTraits.STABLE)
@RestController
public class HelloWorldAuthController {

    /*
     * This endpoint allows anonymous/unauthenticated access.
     *
     * An authorization token may or may not be presented.
     */
    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    @AuthorizeAnonymous
    public String ping() {
        return "pong";
    }


    /*
     * This endpoint figures out which organization the caller is trying to
     * access by looking at the "orgId" URI parameter, from the
     * "/organizations/{orgId}/data" URI pattern described in the
     * @RequestMapping annotation.  The @TargetOrgSource governs where we
     * find the targeted organization:
     *
     *   @TargetOrgSource @PathVariable("orgId") OrgId targetOrgId
     *
     *
     * The two @AuthorizeXXX method-level annotations declare valid
     * authorization criteria.  Both will be evaluated, and if at least one of
     * them matches, access will be granted.
     *
     * @AuthorizeWhen(
     *     role = Role.ID_FULL_ADMIN,
     *     scopes = Scope.Identity.SCIM)
     *
     * The @AuthorizeWhen annotation will check if the caller is *in* the
     * targeted organization, and has the "id_full_admin" role, in addition to
     * presenting the "Identity:SCIM" scope.
     *
     *
     * @AuthorizeDelegationWhen(
     *     roleInManagedOrg = Role.ID_FULL_ADMIN,
     *     scopes = Scope.Identity.SCIM)
     *
     * The @AuthorizeDelegationWhen annotation will check if the caller
     * is allowed to *manage* the targeted organization, has the "id_full_admin"
     * role in that *managed* organization, and that the token presents the
     * "Identity:SCIM" scope.
     *
     *
     * Example:
     *
     * If I have role "id_full_admin" in the "Cisco" organization, present the
     * "Identity:SCIM" scope and try to access "/organizations/cisco-guid/data",
     * I will be granted access.
     *
     * Similarly, if my account is in the "Cisco" organization, but I am also
     * listed as an "id_full_admin" manager for managed organization "Acme", I
     * will be granted access to "/organizations/acme-guid/data", provided my
     * token has the "Identity:SCIM" scope.
     *
     * If I however try to access "/organizations/foo-guid/data", I will be
     * denied access, as I do not have "Foo" in the list of organizations I
     * manage.
     */
    @RequestMapping(value = "/organizations/{orgId}/data1", method = RequestMethod.GET)
    @AuthorizeWhen(
            role = Role.ID_FULL_ADMIN,
            scopes = Scope.Identity.SCIM)
    @AuthorizeDelegationWhen(
            roleInManagedOrg = Role.ID_FULL_ADMIN,
            scopes = Scope.Identity.SCIM)
    public String getDataExample1(
            @TargetOrgSource @PathVariable("orgId") OrgId targetOrgId,
            AuthInfo authInfo) {

        return String.format("I am in org %s, and will manage org %s",
                authInfo.getOrgId(),
                targetOrgId.toString());
    }

    /*
     * Authorization cases can be named, with 'caseName = "xyz"'.
     *
     * The controller code can then decide what to do based on which
     * case matched, by declaring a parameter of type AuthResult. The
     * parameter will be populated automatically.
     */
    @RequestMapping(
            value = "/organizations/{orgId}/data2",
            method = RequestMethod.GET)
    @AuthorizeWhen(
            role = Role.ID_FULL_ADMIN,
            scopes = Scope.Identity.SCIM,
            caseName = "privileged")
    @AuthorizeWhen(
            role = Role.ANY,
            scopes = Scope.Identity.SCIM)
    public String getDataExample2(
            @TargetOrgSource @PathVariable("orgId") OrgId targetOrgId,
            AuthInfo authInfo,
            AuthResult matches) {

        if (matches.getMatchingCases().contains("privileged")) {
            return String.format(
                    "For privileged eyes only, looking at %s as %s",
                    targetOrgId,
                    authInfo.getEffectiveUser());
        } else {
            return String.format(
                    "Something less privileged, looking at %s as %s",
                    targetOrgId,
                    authInfo.getEffectiveUser());
        }
    }

    /*
     * This endpoint allows all authenticated users/machines/services,
     * completely disregarding account roles and token scope.
     *
     * The targeted organization source is set to match whatever
     * organization is in the AuthInfo parameter, essentially meaning that
     * you are granted access to your own organization.
     *
     * The AuthInfo parameter is be populated automatically.
     */
    @RequestMapping(value = "/organizations/data3", method = RequestMethod.GET)
    @AuthorizeWhen(role = Role.ANY, scopes = Scope.ANY)
    public String getDataExample3(
            @TargetOrgSource AuthInfo authInfo) {

        return "Accessing '/data' as " + authInfo.getEffectiveUser();
    }

    /*
     * In this case, the caller account must have the "id_full_admin" role, the
     * token must have the "Identity:SCIM" scope, and the caller account must
     * be in the "Cisco" organization.  The targeted organization ID is, this
     * time, coming from a request parameter (the @TargetOrgSource is
     * attached to a @RequestParam).
     *
     * Example:
     *
     * I will be given access if I have the "id_full_admin" role, present the
     * "Identity:SCIM" scope, am in the "Cisco" organization, and perform
     * HTTP GET "/organizations/data?orgId=cisco-guid".
     */
    @RequestMapping(value = "/organizations/data4", method = RequestMethod.GET)
    @AuthorizeWhen(
            role = Role.ID_FULL_ADMIN,
            scopes = Scope.Identity.SCIM,
            userOrgId = Org.CISCO)
    public String getDataExample4(
            @TargetOrgSource @RequestParam("orgId") OrgId targetOrgId,
            AuthInfo authInfo) {

        return String.format("Accessing %s as %s",
                targetOrgId,
                authInfo.getEffectiveUser());
    }

    /*
     * This endpoint allows access to callers that have the "id_full_admin" role
     * in the targeted *managed* organization, present the "Identity:SCIM" scope
     * and that are part of Cisco.  The target organization ID is again given by
     * a request parameter, "orgId".
     *
     * Example:
     *
     * I am a non-admin user in the Cisco organization, but am an
     * "id_full_admin" in a Cisco-managed test organization "Acme".  I present
     * the "Identity:SCIM" scope; access will be granted.
     *
     * If I am trying to access the Cisco organization, though, access won't be
     * granted.
     */
    @RequestMapping(value = "/organizations/data5", method = RequestMethod.GET)
    @AuthorizeDelegationWhen(
            roleInManagedOrg = Role.ID_FULL_ADMIN,
            scopes = Scope.Identity.SCIM,
            userOrgId = Org.CISCO)
    public String getDataExample5(
            @TargetOrgSource @RequestParam("orgId") OrgId targetOrgId,
            AuthInfo authInfo) {

        return String.format("Accessing %s as %s",
                targetOrgId,
                authInfo.getEffectiveUser());
    }

    /*
     * The caller must be attempting to access a resource from its *own*
     * organization, have the "id_full_admin" role in that organization, the
     * token must have the "Identity:SCIM" scope, and the caller must be a
     * machine account.
     */
    @RequestMapping(value = "/organizations/{orgId}/data6", method = RequestMethod.GET)
    @AuthorizeWhen(
            role = Role.ID_FULL_ADMIN,
            scopes = Scope.Identity.SCIM,
            accountType = AccountType.MACHINE)
    public String getDataExample6(
            @TargetOrgSource @PathVariable("orgId") OrgId targetOrgId,
            AuthInfo authInfo) {

        return String.format("Accessing %s as %s",
                targetOrgId,
                authInfo.getEffectiveUser());
    }

    /*
     * The caller is allowed to access resources in all organizations, given
     * that it has the "id_full_admin" role, the token has the "Identity:SCIM"
     * scope, and the caller is part of the Cisco org.
     *
     * Setting crossOrg = true disables the target org checking that is normally
     * associated with the @TargetOrgSource annotation.
     */
    @RequestMapping(value = "/organizations/{orgId}/data7", method = RequestMethod.GET)
    @AuthorizeWhen(
            role = Role.ID_FULL_ADMIN,
            scopes = Scope.Identity.SCIM,
            userOrgId = Org.CISCO,
            crossOrg = true)
    public String getDataExample7(
            @TargetOrgSource @PathVariable("orgId") OrgId targetOrgId,
            AuthInfo authInfo) {

        return String.format("Accessing %s as %s",
                targetOrgId,
                authInfo.getEffectiveUser());
    }

    /*
     * This endpoint allows all authenticated users/machines/services,
     * completely disregarding the presented roles and scopes. The
     * @AuthorizeCustom annotation should only be used when other annotations
     * cannot cover the authorization requirements and the developer has no
     * other option than implementing custom authorization.
     *
     * The idea is to document that this endpoint has implemented custom
     * authorization.  By using a specific annotation for this, such endpoints
     * can be easily found and reviewed.
     */
    @RequestMapping(value = "/organizations/{orgId}/data8", method = RequestMethod.GET)
    @AuthorizeCustom
    public String getDataExample8(
            @PathVariable("orgId") OrgId targetOrgId,
            AuthInfo authInfo,
            HttpServletRequest request) {

        // .verify(request) will throw a ServerException.forbidden() if no
        // condition matches, and return an AuthResult if authorized.
        //
        // .matches(authInfo) will return an AuthResult, but won't throw.
        //
        // Each 'require' block is OR-ed with the other 'require' blocks.
        // Conditions inside 'require' blocks are AND-ed together.
        // This matches the annotation semantics.
        AuthBuilder
                .require(isMachineAccount(), hasScopes(Scope.Identity.SCIM))
                .orRequire(isInOrganization(Org.CISCO))
                .verify(request);

        return String.format("Accessing %s as %s",
                targetOrgId,
                authInfo.getEffectiveUser());
    }

    /*
     * When @AuthorizePending is present, the @AuthorizeWhen and
     * @AuthorizeDelegationWhen annotations will be evaluated, but no action
     * will be taken if no match is found.
     *
     * When using @AuthorizePending, the @AuthorizeWhen and
     * @AuthorizeDelegationWhen annotations can be added to a controller method
     * without affecting its authorization logic.
     *
     * If the @AuthorizeXXX annotations and the existing controller method logic
     * do not reach the same conclusion, the discrepancy will be logged.
     * This allows to quickly identity mismatches when transitioning to the
     * @AuthorizeXXX annotation family.
     */
    @RequestMapping(value = "/organizations/{orgId}/data9", method = RequestMethod.GET)
    @AuthorizationScope({"webex-squared:admin", "Identity:SCIM"})
    @AuthorizationRole("id_full_admin")
    @AuthorizePending
    @AuthorizeWhen(role = Role.ID_FULL_ADMIN, scopes = Scope.WebexSquared.ADMIN)
    @AuthorizeWhen(role = Role.ID_FULL_ADMIN, scopes = Scope.Identity.SCIM)
    public String getDataExample9(
            @TargetOrgSource @PathVariable("orgId") OrgId targetOrgId,
            AuthInfo authInfo) {

        return String.format("Accessing %s as %s",
                targetOrgId,
                authInfo.getEffectiveUser());
    }
}
