'use strict';

angular.module('subutai.environment.service', [])
	.factory('environmentService', environmentService);


environmentService.$inject = ['$http', '$q'];

function environmentService($http, $q) {

	var ENVIRONMENTS_URL = SERVER_URL + 'rest/ui/environments/';
	var NOT_REGISTERED_CONTAINERS_URL = SERVER_URL + 'rest/ui/local/containers/notregistered';

	var ENVIRONMENT_START_BUILD = ENVIRONMENTS_URL + 'build/';
	var ENVIRONMENT_ADVANCED_BUILD = ENVIRONMENTS_URL + 'build/advanced';

	var CONTAINERS_URL = ENVIRONMENTS_URL + 'containers/';
	var CONTAINER_TYPES_URL = CONTAINERS_URL + 'types/';

	var VERIFIED_TEMPLATE_URL = ENVIRONMENTS_URL + 'templates/verified/';

	var PEERS_URL = ENVIRONMENTS_URL + 'peers/';

	var RH_URL = ENVIRONMENTS_URL + 'resourcehosts/';

	var TENANTS_URL = ENVIRONMENTS_URL + 'tenants';


	var environmentService = {
		getVerifiedTemplate: getVerifiedTemplate,

		getEnvironments : getEnvironments,
		getTenants: getTenants,
		startEnvironmentAdvancedBuild : startEnvironmentAdvancedBuild,
		startEnvironmentAutoBuild: startEnvironmentAutoBuild,
		destroyEnvironment: destroyEnvironment,
		modifyEnvironment: modifyEnvironment,


		setSshKey : setSshKey,
		getSshKey : getSshKey,
		removeSshKey : removeSshKey,


		getContainerStatus : getContainerStatus,
		getContainerSnapshots : getContainerSnapshots,
		removeContainerSnapshot: removeContainerSnapshot,
		rollbackContainerToSnapshot: rollbackContainerToSnapshot,
		addContainerSnapshot: addContainerSnapshot,
		destroyContainer : destroyContainer,
		switchContainer : switchContainer,
		setContainerName : setContainerName,
		createTemplate : createTemplate,


		getContainersType : getContainersType,
		getContainersTypesInfo : getContainersTypesInfo,
		setTags : setTags,
		removeTag : removeTag,


		getEnvQuota: getEnvQuota,
		updateQuota: updateQuota,

        getUploadProgress: getUploadProgress,

		getPeers : getPeers,

		getResourceHosts: getResourceHosts,


		getShared: getShared,
		share: share,

		revoke: revoke,

		getInstalledPlugins: getInstalledPlugins,

		getNotRegisteredContainers: getNotRegisteredContainers,
		deleteNotRegisteredContainer: deleteNotRegisteredContainer,

		getServerUrl : function getServerUrl() { return ENVIRONMENTS_URL; },
		getTenantsUrl : function getTenantsUrl() { return TENANTS_URL; }
	};

	return environmentService;



	//// Implementation

	function getVerifiedTemplate(name){
        return $http.get(VERIFIED_TEMPLATE_URL + name, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getUploadProgress(templateName) {
		return $http.get(SERVER_URL + "rest/v1/peer/templatesprogress/" + templateName);
	}


	function getEnvironments() {
		return $http.get(ENVIRONMENTS_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getTenants() {
		return $http.get(TENANTS_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}


	function startEnvironmentAutoBuild(environmentName, containers) {
		var postData = 'name=' + environmentName + "&topology=" + containers;
		return $http.post(
			ENVIRONMENT_START_BUILD,
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function startEnvironmentAdvancedBuild(environmentName, containers) {
		var postData = 'name=' + environmentName + "&topology=" + containers;
		return $http.post(
			ENVIRONMENT_ADVANCED_BUILD,
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function destroyEnvironment(environmentId) {
		return $http.delete(ENVIRONMENTS_URL + environmentId);
	}

	function modifyEnvironment(containers, advanced) {
		if(advanced == undefined || advanced == null) advanced = '';
		var postData = 'topology=' + JSON.stringify( containers.topology )
			+ '&removedContainers=' + JSON.stringify( containers.removedContainers )
			+ '&quotaContainers=' + JSON.stringify( containers.changingContainers );

		return $http.post(
			ENVIRONMENTS_URL + containers.environmentId + '/modify/' + advanced,
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function switchContainer(containerId, type) {
		return $http.put(
			CONTAINERS_URL + containerId + '/' + type,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function getContainerStatus(containerId) {
		return $http.get(
			CONTAINERS_URL + containerId + '/state',
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

	function getContainerSnapshots(containerId) {
		return $http.get(
			CONTAINERS_URL + containerId + '/snapshots',
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

	function removeContainerSnapshot(containerId, partition, label){
        return $http.delete(
            CONTAINERS_URL + containerId + '/snapshots/partition/' + partition + '/label/' + label,
            {withCredentials: true}
        );
	}

	function rollbackContainerToSnapshot(containerId, partition, label){
        return $http.put(
            CONTAINERS_URL + containerId + '/snapshots/partition/' + partition + '/label/' + label,
            {withCredentials: true}
        );
	}

	function addContainerSnapshot(containerId, partition, label, stopContainer){
        return $http.post(
            CONTAINERS_URL + containerId + '/snapshots/partition/' + partition + '/label/' + label + '/' + stopContainer,
            {withCredentials: true}
        );
	}

	function destroyContainer(containerId) {
		return $http.delete(CONTAINERS_URL + containerId);
	}


	function setSshKey(sshKey, environmentId) {
		var postData = 'key=' + window.btoa(sshKey);
		return $http.post(
			ENVIRONMENTS_URL + environmentId + '/keys',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function getSshKey(environmentId) {
		return $http.get(ENVIRONMENTS_URL + environmentId + '/keys');
	}

	function removeSshKey(environmentId, sshKey) {
		return $http.delete(ENVIRONMENTS_URL + environmentId + '/keys?key=' + window.btoa(sshKey));
	}


	function setContainerName( container, name ) {
		return $http.put( ENVIRONMENTS_URL + container.environmentId + '/containers/' + container.id + '/name' +
			'?name=' + name );
	}

	function createTemplate( container,name, version, isPrivate ) {
	    var URL = ENVIRONMENTS_URL + container.environmentId + '/containers/' + container.id + '/export/' + name + "/" + version + "/" + ( isPrivate == true ? "true" : "false" ) ;
		return $http.post( URL );
	}


	function getContainersType() {
		return $http.get(CONTAINER_TYPES_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getContainersTypesInfo() {
		return $http.get(CONTAINER_TYPES_URL + "info", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}


	function getEnvQuota(containerId) {
		return $http.get(
			CONTAINERS_URL + containerId + '/quota',
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}	

	function updateQuota(containerId, postData) {
		return $http.post(
			CONTAINERS_URL + containerId + '/quota',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function getPeers() {
		return $http.get(PEERS_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getResourceHosts() {
		return $http.get(RH_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function setTags(environmentId, containerId, tags) {
		var postData = 'tags=' + JSON.stringify(tags);
		return $http.post(
			ENVIRONMENTS_URL + environmentId + '/containers/' + containerId + '/tags',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function removeTag(environmentId, containerId, tag) {
		return $http.delete(ENVIRONMENTS_URL + environmentId + '/containers/' + containerId + '/tags/' + tag);		
	}


	function getShared (environmentId) {
		return $http.get (ENVIRONMENTS_URL + "shared/users/" + environmentId);
	}

	function share (users, environmentId) {
		var postData = "users=" + users;
		return $http.post(
			ENVIRONMENTS_URL + environmentId + "/share",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function revoke (environmentId) {
		return $http.put (ENVIRONMENTS_URL + environmentId + "/revoke");
	}

	function getInstalledPlugins() {
		return $http.get(SERVER_URL + 'js/plugins.json', {headers: {'Content-Type': 'application/json'}});
	}

	function getNotRegisteredContainers() {
		return $http.get(NOT_REGISTERED_CONTAINERS_URL, {headers: {'Content-Type': 'application/json'}});
	}

	function deleteNotRegisteredContainer(containerId) {
		return $http.delete(NOT_REGISTERED_CONTAINERS_URL + '/' + containerId);
	}
}
