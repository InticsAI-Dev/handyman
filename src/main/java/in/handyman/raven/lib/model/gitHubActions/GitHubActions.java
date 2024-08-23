package in.handyman.raven.lib.model.gitHubActions;

import in.handyman.raven.exception.HandymanException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;

import org.slf4j.Logger;

public class GitHubActions {


    private final Logger logger;

    public GitHubActions(Logger logger) {
        this.logger = logger;
    }

    // Protected method to create HttpClient instance
    protected HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }

    // Clone the repository from the remote URL
    public Path cloneRepository(String remoteUrl, String localRepoPath, String branchName, String authToken) throws GitAPIException {
        logger.info("Cloning repository from URL: " + remoteUrl);
        logger.info("Target directory: " + localRepoPath);
        logger.info("Branch: " + branchName);

        try {
            Git result = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(new File(localRepoPath))
                    .setBranchesToClone(java.util.Collections.singletonList("refs/heads/" + branchName))
                    .setBranch("refs/heads/" + branchName)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
                    .call();

            logger.info("Repository cloned successfully to " + result.getRepository().getDirectory());
            return Path.of(localRepoPath);
        } catch ( GitAPIException e) {
            logger.error("Failed to clone repository: " + e.getMessage(), e);
            throw new HandymanException("Failed to clone repository: " + e.getMessage(), e);
        }
    }

    public boolean pullChanges(String localRepoPath, String authToken) throws IOException, GitAPIException {
        try (Git git = Git.open(new File(localRepoPath))) {
            PullResult result = git.pull()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
                    .call();
            boolean isSuccess = result.isSuccessful();
            logger.info("Pulled from the remote repository: " + isSuccess);
            return isSuccess;
        } catch (IOException | GitAPIException e) {
            logger.error("Failed to checkout: " + e.getMessage(), e);
            new HandymanException("Failed to pull changes: " + e.getMessage());
            return false;
        }
    }

    // Add, commit, and push changes to the remote repository
    public boolean pushChanges(String localRepoPath, String authToken) throws IOException, GitAPIException {
        try (Git git = Git.open(new File(localRepoPath))) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Commit message").call();
            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
                    .call();
            logger.info("Pushed to the remote repository.");
            return true;
        } catch (IOException | GitAPIException e) {
            new HandymanException("Failed to push changes: " + e.getMessage());
            return false;
        }
    }
    public boolean checkout(String localRepoPath, String branchOrCommit, String authToken) throws IOException, GitAPIException {
        try (Git git = Git.open(new File(localRepoPath))) {
            Ref ref = git.checkout().setName(branchOrCommit).call();
            logger.info("Checked out to: " + ref.getName());
            return true;
        } catch (IOException | GitAPIException e) {
            logger.error("Failed to checkout: " + e.getMessage(), e);
            throw new HandymanException("Failed to checkout: " + e.getMessage(), e);
        }
    }
}
