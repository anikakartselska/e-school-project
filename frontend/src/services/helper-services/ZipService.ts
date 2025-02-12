import JSZip from "jszip";

export async function createZipFromFiles(files: File[]): Promise<Blob> {
    const zip = new JSZip(); // Create a new zip instance

    // Add files to the zip
    files.forEach((file, index) => {
        zip.file(file.name + "_" + index, file);  // Adds file to zip with the original filename
    });

    // Generate the zip file as a Blob
    const zipBlob = await zip.generateAsync({type: "blob"});

    return zipBlob; // Return the Blob containing the ZIP file
}

// export async function createZipFromSmsFilesWithEvaluationIds(files: SmsFileWithEvaluationIds[]): Promise<Blob> {
//     const zip = new JSZip(); // Create a new zip instance
//
//     // Add files to the zip
//     files.forEach(file => {
//         zip.file(file.fileContent.name, new Blob([JSON.stringify(file.evaluationIds), file.fileContent]));  // Adds file to zip with the original filename
//     });
//
//     // Generate the zip file as a Blob
//     const zipBlob = await zip.generateAsync({type: "blob"});
//
//     return zipBlob; // Return the Blob containing the ZIP file
// }