import { useRef, type KeyboardEvent } from 'react';

type MessageComposerProps = {
  acceptsImages: boolean;
  disabled: boolean;
  prompt: string;
  files: File[];
  placeholder: string;
  onPromptChange: (value: string) => void;
  onFilesChange: (files: File[]) => void;
  onSubmit: () => void;
};

export function MessageComposer({
  acceptsImages,
  disabled,
  prompt,
  files,
  placeholder,
  onPromptChange,
  onFilesChange,
  onSubmit,
}: MessageComposerProps) {
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  function handleKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      onSubmit();
    }
  }

  return (
    <div className="composer-card">
      {files.length > 0 ? (
        <div className="upload-strip">
          {files.map((file) => (
            <div key={`${file.name}-${file.lastModified}`} className="upload-chip">
              <span>{file.name}</span>
              <button
                type="button"
                onClick={() => onFilesChange(files.filter((item) => item !== file))}
              >
                Remove
              </button>
            </div>
          ))}
        </div>
      ) : null}

      <div className="composer-row">
        {acceptsImages ? (
          <>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              multiple
              hidden
              onChange={(event) => {
                onFilesChange(Array.from(event.target.files ?? []));
                event.target.value = '';
              }}
            />
            <button
              type="button"
              className="icon-button"
              onClick={() => fileInputRef.current?.click()}
              disabled={disabled}
            >
              Add images
            </button>
          </>
        ) : null}

        <textarea
          value={prompt}
          onChange={(event) => onPromptChange(event.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          disabled={disabled}
          rows={1}
        />

        <button type="button" className="send-button" onClick={onSubmit} disabled={disabled}>
          Send
        </button>
      </div>
    </div>
  );
}
