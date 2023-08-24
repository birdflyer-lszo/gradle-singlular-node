package com.brunoritz.gradle.singularnode.platform;

import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayout;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Installs the requested version of NodeJS into the installation directory. Removes the top-level directory from the
 * NodeJS archive since it is not relevant for the installation and simplifies further consumption of the software.
 * <p>
 * For those distributions packages in {@code .tar.gz}, additional steps are taken to repair the broken {@code npm}
 * and {@code npx} symlinks. Gradle does not preserve symlinks when extracting TAR GZ archives.
 * <p>
 * Any existing installation will be deleted prior to the installation.
 */
public abstract class InstallNodeJsTask
	extends DefaultTask
{
	private final FileSystemOperations files;
	private final ArchiveOperations archives;

	@Inject
	public InstallNodeJsTask(FileSystemOperations files, ArchiveOperations archives)
	{
		this.files = files;
		this.archives = archives;
	}

	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract RegularFileProperty getNodeArchive();

	@Internal
	public abstract Property<InstallationLayout> getInstallationLayout();

	@OutputDirectory
	public abstract DirectoryProperty getNodeJsInstallDir();

	@TaskAction
	public void installNode()
		throws IOException
	{
		InstallationLayout layout = getInstallationLayout().get();

		cleanTarget(layout);
		extractArchive(layout);
	}

	private void cleanTarget(InstallationLayout layout)
	{
		files.delete(spec -> spec.delete(layout.nodeJsInstallDir()));
	}

	private void extractArchive(InstallationLayout layout)
		throws IOException
	{
		RegularFile nodeArchive = getNodeArchive().get();

		if (nodeArchive.getAsFile().getName().endsWith(".zip")) {
			extractArchive(archives.zipTree(nodeArchive), layout);
		} else {
			extractArchive(archives.tarTree(nodeArchive), layout);
			restoreBrokenSymlinks(layout);
		}
	}

	private void extractArchive(FileTree source, InstallationLayout layout)
	{
		files.copy(cp -> {
			cp.from(source);
			cp.into(layout.nodeJsInstallDir());
			cp.eachFile(fileCopy -> {
				String path = fileCopy.getPath();
				String sanitizedPath = path.substring(path.indexOf("/") + 1);

				fileCopy.setPath(sanitizedPath);
			});
		});
	}

	private void restoreBrokenSymlinks(InstallationLayout layout)
		throws IOException
	{
		if (!(layout.nodeJsBinDirectory().exists() || layout.nodeJsBinDirectory().mkdirs())) {
			throw new IOException("Failed to create NodeJS bin directory");
		}

		if (!(layout.pathOfBundledNpmScript().delete() && layout.pathOfBundnledNpxScript().delete())) {
			throw new IOException("Failed to delete broken symlinks");
		}

		Files.createSymbolicLink(
			layout.pathOfBundledNpmScript().toPath(),
			layout.pathOfBundledCliScript("npm").toPath()
		);

		Files.createSymbolicLink(
			layout.pathOfBundnledNpxScript().toPath(),
			layout.pathOfBundledCliScript("npx").toPath()
		);
	}
}
